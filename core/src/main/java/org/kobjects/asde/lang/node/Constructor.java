package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.classifier.ClassPropertyDescriptor;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.asde.lang.program.GlobalSymbol;
import org.kobjects.asde.lang.classifier.InstantiableType;
import org.kobjects.asde.lang.statement.UninitializedField;
import org.kobjects.typesystem.PropertyDescriptor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constructor extends Node {
  final String name;
  InstantiableType instantiableType;
  Map<String, Integer> nameIndexMap;
  int[] indexMap;
  int arraySize;

  public Constructor(String name, Map<String, Integer> nameIndexMap, Node... children) {
    super(children);
    this.name = name;
    this.nameIndexMap = nameIndexMap;
    indexMap = new int[nameIndexMap.size()];
  }

  public static Node create(Node base, List<Node> arguments) {
    if (!(base instanceof Identifier)) {
      throw new RuntimeException("Identifier expected before '{'");
    }
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
    return new Constructor(((Identifier) base).name, propertyIndexMap, children);
  }

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
    GlobalSymbol symbol = resolutionContext.program.getSymbol(name);
    if (symbol == null) {
      throw new RuntimeException("'" + name + "' is not defined");
    }
    symbol.validate(resolutionContext.programValidationContext);
    Object value = symbol.getValue();
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

  @Override
  public Object eval(EvaluationContext evaluationContext) {
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
  public InstantiableType returnType() {
    return instantiableType;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, name, errors);
    asb.append('{');
    boolean first = true;
    for (Map.Entry<String,Integer> entry : nameIndexMap.entrySet()) {
      if (first) {
        first = false;
      } else {
        asb.append(", ");
      }
      asb.append(entry.getKey());
      asb.append(" = ");
      children[entry.getValue()].toString(asb, errors, preferAscii);
    }
    asb.append('}');
  }
}