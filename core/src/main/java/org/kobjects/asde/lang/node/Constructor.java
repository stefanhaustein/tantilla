package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.array.Array;
import org.kobjects.asde.lang.array.ArrayType;
import org.kobjects.asde.lang.classifier.ClassPropertyDescriptor;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.asde.lang.program.GlobalSymbol;
import org.kobjects.asde.lang.classifier.InstantiableType;
import org.kobjects.asde.lang.statement.UninitializedField;
import org.kobjects.typesystem.PropertyDescriptor;
import org.kobjects.typesystem.Type;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constructor extends Node {
  final String name;
  final boolean isArrayLiteral;
  InstantiableType instantiableType;
  Type elementType;
  Map<String, Integer> nameIndexMap;
  int[] indexMap;
  int arraySize;

  public Constructor(String name, Map<String, Integer> nameIndexMap, Node... children) {
    super(children);
    this.name = name;
    this.nameIndexMap = nameIndexMap;
    isArrayLiteral = nameIndexMap == null;
    indexMap = isArrayLiteral ? null : new int[nameIndexMap.size()];
  }

  public static Node create(Node base, List<Node> arguments) {
    String name = base.toString();
    if (name.endsWith("()")) {
      return new Constructor(name.substring(0, name.indexOf('(')), null, arguments.toArray(new Node[0]));
    }
    System.err.println("Constructor.create --- name: " + name);
    Node[] children = new Node[arguments.size()];
    HashMap<String, Integer> propertyIndexMap = new HashMap<>();
    for (int i = 0; i < arguments.size(); i++) {
      if (!(arguments.get(i) instanceof RelationalOperator)) {
        throw new RuntimeException("Assignment expected; got: '" + arguments.get(i) + "' (" + arguments.get(i).getClass().getSimpleName() + ")");
      }
      RelationalOperator relationalOperator = (RelationalOperator) arguments.get(i);
      if (!relationalOperator.getName().equals("=")) {
        throw new RuntimeException("Assignment expected; got: '" + relationalOperator.getName() + "'");
      }
      if (!(relationalOperator.children[0] instanceof Identifier)) {
        throw new RuntimeException("Identifier expecte; got: " + relationalOperator.children[0]);
      }
      children[i] = relationalOperator.children[1];
      propertyIndexMap.put(((Identifier) relationalOperator.children[0]).name, i);
    }
    return new Constructor(base.toString(), propertyIndexMap, children);
  }

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line) {
    GlobalSymbol symbol = resolutionContext.program.getSymbol(name);
    if (symbol == null) {
      throw new RuntimeException("'" + name + "' is not defined");
    }
    symbol.validate(resolutionContext.programValidationContext);
    Object value = symbol.getValue();

    if (isArrayLiteral) {
      if (!(value instanceof Type)) {
        throw new RuntimeException(name + " is not a type.");
      }
      elementType = (Type) value;
      for (Node child : children) {
        if (!elementType.isAssignableFrom(child.returnType())) {
          throw new RuntimeException("Expected type '" + elementType + "' but got '" + child.returnType() + " for child node: " + child);
        }
      }
    } else {
      if (!(value instanceof InstantiableType)) {
        throw new RuntimeException("'" + name + "' is not an instantiable type.");
      }
      instantiableType = (InstantiableType) value;

      Arrays.fill(indexMap, -1);
      arraySize = 0;

      for (PropertyDescriptor propertyDescriptor : instantiableType.getPropertyDescriptors()) {
        ClassPropertyDescriptor descriptor = (ClassPropertyDescriptor) propertyDescriptor;
        if (descriptor.getInitializer() != null) {
          Integer childIndex = nameIndexMap.get(descriptor.getName());
          if (childIndex != null) {
            if (!propertyDescriptor.type().isAssignableFrom(children[childIndex].returnType())) {
              throw new RuntimeException("Expected type for property " + descriptor.getName() + ": " + propertyDescriptor.type() + "; got: " + children[childIndex].returnType());
            }
            indexMap[childIndex] = descriptor.getIndex();
            arraySize = Math.max(descriptor.getIndex() + 1, arraySize);
          } else if (descriptor.getInitializer() instanceof UninitializedField) {
            throw new RuntimeException("Initializer required for property " + descriptor.getName());
          }
        }
      }
      for (int i = 0; i < indexMap.length; i++) {
        if (indexMap[i] == -1) {
          throw new RuntimeException("Imitializer property " + i + " is not a part of class " + instantiableType);
        }
      }
    }
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    if (isArrayLiteral) {
      Object[] values = new Object[children.length];
      for (int i = 0; i < children.length; i++) {
        values[i] = children[i].eval(evaluationContext);
      }
      return new Array(elementType, values);
    }
    if (arraySize == 0) {
      return instantiableType.createInstance(evaluationContext);
    }
    Object[] params = new Object[arraySize];
    for (int i = 0; i < children.length; i++) {
      params[indexMap[i]] = children[i].eval(evaluationContext);
    }
    return instantiableType.createInstance(evaluationContext, params);
  }

  @Override
  public Type returnType() {
    return isArrayLiteral ? new ArrayType(elementType) : instantiableType;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, name, errors);
    if (isArrayLiteral) {
      asb.append("[]{");
      for (int i = 0; i < children.length; i++) {
        if (i > 0) {
          asb.append(", ");
        }
        children[i].toString(asb, errors, preferAscii);
      }
    } else {
      asb.append('{');
      boolean first = true;
      for (Map.Entry<String, Integer> entry : nameIndexMap.entrySet()) {
        if (first) {
          first = false;
        } else {
          asb.append(", ");
        }
        asb.append(entry.getKey());
        asb.append(" = ");
        children[entry.getValue()].toString(asb, errors, preferAscii);
      }
    }
    asb.append('}');
  }
}
