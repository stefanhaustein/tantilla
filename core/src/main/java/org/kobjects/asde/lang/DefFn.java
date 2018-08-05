package org.kobjects.asde.lang;

import org.kobjects.asde.lang.node.FnCall;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.node.Operator;
import org.kobjects.asde.lang.node.Variable;
import org.kobjects.asde.lang.type.FunctionType;
import org.kobjects.asde.lang.type.Parameter;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.type.Typed;

import java.lang.annotation.Inherited;

public class DefFn implements Typed {
  Program program;
  Node expression;
  FunctionType type;

  public DefFn(Program program, FnCall target, Node expression) {
    this.program = program;

    Parameter[] parameters = new Parameter[target.children.length];
    for (int i = 0; i < parameters.length; i++) {
      Node param = target.children[i];
      if (!(param instanceof Variable) || param.children.length != 0) {
        throw new RuntimeException("parameter name expected, got " + param);
      }
      String name = ((Variable) param).name;
      Type type = name.endsWith("$") ? Type.STRING : Type.NUMBER;
      parameters[i] = new Parameter(name, type);
    }
    this.expression = expression;
    type = new FunctionType(target.name.endsWith("$") ? Type.STRING : Type.NUMBER, parameters);
  }

  public Object eval(Interpreter interpreter, Object[] parameterValues) {
    Symbol[] saved = new Symbol[type.getParameterCount()];
    for (int i = 0; i < type.getParameterCount(); i++) {
      String param = type.getParameter(i).name;
      saved[i] = program.getSymbol(param);
      program.setSymbol(param, new Symbol(parameterValues[i]));
    }
    try {
      return expression.eval(interpreter);
    } finally {
      for (int i = 0; i < type.getParameterCount(); i++) {
        program.setSymbol(type.getParameter(i).name, saved[i]);
      }
    }
  }

  @Override
  public FunctionType getType() {
    return type;
  }

  @Override
  public String toString() {
    return expression.toString();
  }
}
