package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.DefFn;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Symbol;
import org.kobjects.asde.lang.type.Type;

import java.util.TreeMap;

// Not static for access to the variables.
public class Call extends AssignableNode {
  final Program program;
  public final String name;
  final boolean dollar;

  public Call(Program program, String name, Node... children) {
    super(children);
    this.program = program;
    dollar = name.endsWith("$");
    this.name = name;
  }

  public void set(Interpreter interpreter, Object value) {
    Symbol symbol = program.getSymbol(name);
    if (symbol == null) {
      symbol = new Symbol(null);
      program.setSymbol(name, symbol);
    }

    TreeMap<Integer, Object> target = (TreeMap<Integer, Object>) symbol.value;
    if (target == null) {
      target = new TreeMap<>();
      symbol.value = target;
    }
    for (int i = 0; i < children.length - 1; i++) {
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
    } else {
      if (symbol.value instanceof DefFn) {
        Object[] params = new Object[children.length];
        for (int i = 0; i < params.length; i++) {
          params[i] = children[i].eval(interpreter);
        }
        result = ((DefFn) symbol.value).eval(interpreter, params);
      } else if (children.length == 0) {
        result = symbol.value;
      } else {
        TreeMap<Integer, Object> arr = (TreeMap<Integer, Object>) symbol.value;
        for (int i = 0; i < children.length - 1 && arr != null; i++) {
          arr = (TreeMap<Integer, Object>) arr.get((int) evalDouble(interpreter, i));
        }
        result = arr == null ? null : arr.get((int) evalDouble(interpreter, children.length - 1));
      }
    }
    return result == null ? dollar ? "" : 0.0 : result;
  }

  public Type returnType() {
    return dollar ? Type.STRING : Type.NUMBER;
  }

  public String toString() {
    return name + "(" + super.toString() + ")";
  }
}
