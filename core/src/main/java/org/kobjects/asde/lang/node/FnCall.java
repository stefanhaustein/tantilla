package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.DefFn;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Symbol;
import org.kobjects.asde.lang.type.FunctionType;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.type.Typed;

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
    Symbol symbol = program.getSymbol(name);
    if (symbol == null) {
      throw new RuntimeException("Undefined function: " + name);
    }
    if (!(symbol.value instanceof DefFn)) {
      throw new RuntimeException("symbol is not a function");
    }
    DefFn function = (DefFn) symbol.value;
    Object[] params = new Object[children.length];
    for (int i = 0; i < params.length; i++) {
      params[i] = children[i].eval(interpreter);
    }
    return function.eval(interpreter, params);
  }

  public Type returnType() {
    return name.endsWith("$") ? Type.STRING : Type.NUMBER;
  }

  public String toString() {
    return children.length == 0 ? name : name + "(" + super.toString() + ")";
  }
}
