package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.function.Parameter;

public class InvocationResolver {

  static Node[] resolve(FunctionType signature, Node[] children, int offset, boolean permitPositional) {
    if (signature.getParameterCount() < children.length - offset) {
      throw new RuntimeException("Too many parameters. Expected " + signature.getParameterCount() + " but got " + children.length);
    }

    Node[] result = new Node[signature.getParameterCount()];
    for (int i = 0; i < children.length - offset; i++) {
      Node value = children[i + offset];
      int parameterIndex;
      if (value instanceof Named) {
        permitPositional = false;
        Named named = (Named) value;
        parameterIndex = signature.getParameterIndex(named.name);
        if (parameterIndex == -1) {
          throw new RuntimeException("Parmeter name '" + named.name + "' not found.");
        }
        if (result[parameterIndex] != null) {
          throw new RuntimeException("Parameter '" + named.name + "' assigned already.");
        }
        value = value.children[0];
      } else if (!permitPositional) {
        throw new RuntimeException(i == 0 ? "Positional parameters are not permitted." : "Can't have positional parameters after named parameters");
      } else {
        parameterIndex = i;
      }
      Parameter parameter = signature.getParameter(parameterIndex);
      if (!parameter.getType().isAssignableFrom(value.returnType())) {
        throw new RuntimeException("Cannot assign parameter " + parameter.getName() + " of type " + parameter.getType()
            + " from type " + value.returnType());
      }
      result[parameterIndex] = value;
    }

    for (int i = 0; i < result.length; i++) {
      if (result[i] == null) {
        Parameter parameter = signature.getParameter(i);
        Node defaultValueExpression = parameter.getDefaultValueExpression();
        if (defaultValueExpression == null) {
          throw new RuntimeException("No default value available for parameter " + parameter.getName());
        }
        result[i] = defaultValueExpression;
      }
    }
    return result;
  }
}
