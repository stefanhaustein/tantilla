package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Symbol;
import org.kobjects.asde.lang.type.Type;

import java.util.TreeMap;

// Not static for access to the variables.
public class Identifier extends AssignableNode {
  final Program program;
  public final String name;
  final boolean dollar;

  public Identifier(Program program, String name) {
    this.program = program;
    dollar = name.endsWith("$");
    this.name = name;
  }

  public void set(Interpreter interpreter, Object value) {
    Symbol symbol = program.getSymbol(name);
    if (symbol == null) {
      symbol = new Symbol(value);
      program.setSymbol(name, symbol);
    } else {
      symbol.value = value;
    }
  }

  public Object eval(Interpreter interpreter) {
    Symbol symbol = program.getSymbol(name);
    Object result;
    if (symbol == null) {
      result = null;
    } else {
       result = symbol.value;
    }
    return result == null ? dollar ? "" : 0.0 : result;
  }

  public Type returnType() {
    return dollar ? Type.STRING : Type.NUMBER;
  }

  public String toString() {
    return children.length == 0 ? name : name.substring(0, name.indexOf('[')) + "(" + super.toString() + ")";
  }
}
