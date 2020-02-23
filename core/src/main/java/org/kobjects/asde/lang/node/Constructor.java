package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.classifier.UserProperty;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.PropertyValidationContext;
import org.kobjects.asde.lang.classifier.InstantiableType;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.type.Type;

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
    String name = base.toString();
    System.err.println("Constructor.create --- name: " + name);
    Node[] children = new Node[arguments.size()];
    HashMap<String, Integer> propertyIndexMap = new HashMap<>();
    for (int i = 0; i < arguments.size(); i++) {
      if (!(arguments.get(i) instanceof Colon)) {
        throw new RuntimeException("<field> : <expr> expected; got: '" + arguments.get(i) + "' (" + arguments.get(i).getClass().getSimpleName() + ")");
      }
      Node colon = arguments.get(i);
      children[i] = colon.children[1];
      propertyIndexMap.put(((Identifier) colon.children[0]).name, i);
    }
    return new Constructor(base.toString(), propertyIndexMap, children);
  }

  @Override
  protected void onResolve(PropertyValidationContext resolutionContext, int line) {
    UserProperty symbol = (UserProperty) resolutionContext.program.getSymbol(name);
    if (symbol == null) {
      throw new RuntimeException("'" + name + "' is not defined");
    }
    symbol.validate(resolutionContext);
    Object value = symbol.getStaticValue();

      if (!(value instanceof InstantiableType)) {
        throw new RuntimeException("'" + name + "' is not an instantiable type.");
      }
      instantiableType = (InstantiableType) value;

      Arrays.fill(indexMap, -1);
      arraySize = 0;

      for (Property property : instantiableType.getAllProperties()) {
        if (property instanceof UserProperty) {
          UserProperty descriptor = (UserProperty) property;
          Integer childIndex = nameIndexMap.get(descriptor.getName());
          if (childIndex != null) {
            if (!property.getType().isAssignableFrom(children[childIndex].returnType())) {
              throw new RuntimeException("Expected type for property " + descriptor.getName() + ": " + property.getType() + "; got: " + children[childIndex].returnType());
            }
            indexMap[childIndex] = descriptor.getFieldIndex();
            arraySize = Math.max(descriptor.getFieldIndex() + 1, arraySize);
          } else if (descriptor.getFieldIndex() != -1 && descriptor.getInitializer() == null) {
            throw new RuntimeException("Initializer required for property " + descriptor.getName());
          }
        }
      }
      for (int i = 0; i < indexMap.length; i++) {
        if (indexMap[i] == -1) {
          throw new RuntimeException("Initializer property " + i + " is not a part of class " + instantiableType);
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
  public Type returnType() {
    return instantiableType;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, name, errors);
      asb.append('{');
      boolean first = true;
      for (Map.Entry<String, Integer> entry : nameIndexMap.entrySet()) {
        if (first) {
          first = false;
        } else {
          asb.append(", ");
        }
        asb.append(entry.getKey());
        asb.append(": ");
        children[entry.getValue()].toString(asb, errors, preferAscii);
      }
    asb.append('}');
  }
}
