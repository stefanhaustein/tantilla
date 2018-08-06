package org.kobjects.asde.lang;

import org.kobjects.asde.lang.node.Apply;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.node.Identifier;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Parameter;
import org.kobjects.typesystem.Type;
import org.kobjects.typesystem.Typed;

public class DefFn implements Function {
  Program program;
  Node expression;
  FunctionType type;

  public DefFn(Program program, Apply target, Node expression) {
    this.program = program;

    Parameter[] parameters = new Parameter[target.children.length - 1];
    for (int i = 0; i < parameters.length; i++) {
      Node param = target.children[i + 1];
      if (!(param instanceof Identifier) || param.children.length != 0) {
        throw new RuntimeException("parameter name expected, got " + param);
      }
      String name = ((Identifier) param).name;
      Type type = name.endsWith("$") ? Type.STRING : Type.NUMBER;
      parameters[i] = new Parameter(name, type);
    }
    this.expression = expression;
    type = new FunctionType(((Identifier) target.children[0]).name.endsWith("$") ? Type.STRING : Type.NUMBER, parameters);
  }

  public Object eval(Interpreter interpreter, Object[] parameterValues) {
    Symbol[] saved = new Symbol[type.getParameterCount()];
    for (int i = 0; i < type.getParameterCount(); i++) {
      String param = type.getParameter(i).name;
      saved[i] = program.getSymbol(param);
      program.setSymbol(param, new Symbol(interpreter.getSymbolScope(), parameterValues[i]));
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
