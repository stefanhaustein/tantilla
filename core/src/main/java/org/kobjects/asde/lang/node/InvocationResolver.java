package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.function.Parameter;

public class InvocationResolver {

  static Node[] resolve(Node[] children, int offset, FunctionType signature) {
    if (signature.getParameterCount() < children.length - offset) {
      throw new RuntimeException("Too many parameters. Expected " + signature.getParameterCount() + " but got " + children.length);
    }

    Node[] result = new Node[signature.getParameterCount()];
    for (int i = 0; i < children.length - offset; i++) {
      Parameter parameter = signature.getParameter(i);
      Node child = children[i + offset];
      if (!parameter.getType().isAssignableFrom(child.returnType())) {
        throw new RuntimeException("Cannot assign parameter " + parameter.getName() + " of type " + parameter.getType()
            + " from txpe " + child.returnType());
      }
      result[i] = child;
    }

    for (int i = children.length - offset; i < result.length; i++) {
      Parameter parameter = signature.getParameter(i);
      Node defaultValueExpression = parameter.getDefaultValueExpression();
      if (defaultValueExpression == null) {
        throw new RuntimeException("No default value available for parameter " + parameter.getName());
      }
      result[i] = defaultValueExpression;
    }

    return result;
  }


}
