package org.kobjects.asde.lang;

import org.kobjects.asde.lang.node.FnCall;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.node.Operator;
import org.kobjects.asde.lang.node.Variable;

public class DefFn {
  Program program;
  String[] parameterNames;
  public String name;
  Node expression;

  public DefFn(Program program, Node assignment) {
    this.program = program;
    if (!(assignment instanceof Operator)
        || !((Operator) assignment).name.equals("=")
        || !(assignment.children[0] instanceof FnCall)) {
      throw new RuntimeException("SetLocal to function declaration expected.");
    }
    FnCall target = (FnCall) assignment.children[0];
    this.name = target.name;
    parameterNames = new String[target.children.length];
    for (int i = 0; i < parameterNames.length; i++) {
      Node param = target.children[i];
      if (!(param instanceof Variable) || param.children.length != 0) {
        throw new RuntimeException("parameter name expected, got " + param);
      }
      parameterNames[i] = ((Variable) param).name;
    }
    expression = assignment.children[1];
  }

  public Object eval(Interpreter interpreter, Object[] parameterValues) {
    Object[] saved = new Object[parameterNames.length];
    for (int i = 0; i < parameterNames.length; i++) {
      String param = parameterNames[i];
      synchronized (program.variables) {
        saved[i] = program.variables.get(param);
        program.variables.put(param, parameterValues[i]);
      }
    }
    try {
      return expression.eval(interpreter);
    } finally {
      for (int i = 0; i < parameterNames.length; i++) {
        synchronized (program.variables) {
          program.variables.put(parameterNames[i], saved[i]);
        }
      }
    }
  }
}
