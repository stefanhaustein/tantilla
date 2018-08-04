package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.DefFn;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.type.Type;

// User-defined function
public class FnCall extends Node {
  final Program program;
  public final String name;

  public FnCall(Program program, String name, Node... children) {
    super(children);
    this.program = program;
    this.name = name;
  }

  public Object eval(Interpreter interpreter) {
    DefFn def = program.functionDefinitions.get(name);
    if (def == null) {
      throw new RuntimeException("Undefined function: " + name);
    }
    Object[] params = new Object[children.length];
    for (int i = 0; i < params.length; i++) {
      params[i] = children[i].eval(interpreter);
    }
    return def.eval(interpreter, params);
  }

  public Type returnType() {
    return name.endsWith("$") ? Type.STRING : Type.NUMBER;
  }

  public String toString() {
    return children.length == 0 ? name : name + "(" + super.toString() + ")";
  }
}
