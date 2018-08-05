package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Symbol;
import org.kobjects.asde.lang.type.Type;

import java.util.TreeMap;

// Not static for access to the variables.
public class Variable extends AssignableNode {
  final Program program;
  public final String name;
  final boolean dollar;

  public Variable(Program program, String name, Node... children) {
    super(children);
    this.program = program;
    dollar = name.endsWith("$");
    this.name = children.length == 0 ? name : name + '[' + children.length + ']';
  }

  public void set(Interpreter interpreter, Object value) {
    Symbol symbol = program.getSymbol(name);
    if (symbol == null) {
      symbol = new Symbol(null);
      program.setSymbol(name, symbol);
    }

    if (children.length == 0) {
      symbol.value = value;
      return;
    }

    TreeMap<Integer, Object> target = (TreeMap<Integer, Object>) symbol.value;
    if (target == null) {
      target = new TreeMap<>();
      symbol.value = target;
    }
    for (int i = 0; i < children.length - 2; i++) {
      int index = (int) evalDouble(interpreter, i);
      TreeMap<Integer, Object> sub = (TreeMap<Integer, Object>) target.get(index);
      if (sub == null) {
        sub = new TreeMap<>();
        target.put(index, sub);
      }
      target = sub;
    }
    target.put((int) evalDouble(interpreter, children.length - 1), value);
  }

  public Object eval(Interpreter interpreter) {
    Symbol symbol = program.getSymbol(name);
    Object result;
    if (symbol == null) {
      result = null;
    } else if (children.length == 0) {
       result = symbol.value;
    } else {
      TreeMap<Integer, Object> arr = (TreeMap<Integer, Object>) symbol.value;
      for (int i = 0; i < children.length - 2 && arr != null; i++) {
        arr = (TreeMap<Integer, Object>) arr.get((int) evalDouble(interpreter, i));
      }
      result = arr == null ? null : arr.get((int) evalDouble(interpreter,children.length - 1));
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
