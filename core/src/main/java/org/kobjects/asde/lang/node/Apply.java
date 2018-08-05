package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.Function;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Symbol;
import org.kobjects.asde.lang.type.Type;

import java.util.TreeMap;

// Not static for access to the variables.
public class Apply extends AssignableNode {
  final Program program;
  public final Node base;

  public Apply(Program program, Node base, Node... children) {
    super(children);
    this.base = base;
    this.program = program;
  }

  public void set(Interpreter interpreter, Object value) {
    if (!(base instanceof Identifier)) {
        throw new RuntimeException("Not assignable");
    }
    String name = ((Identifier) base).name;

    Symbol symbol = program.getSymbol(name);
    if (symbol == null) {
      symbol = new Symbol(interpreter.getSymbolScope(), null);
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
    Object value = base.evalRaw(interpreter);
    Object result;
    if (value == null) {
      result = null;
    } else {
      if (value instanceof Function) {
        Object[] params = new Object[children.length];
        for (int i = 0; i < params.length; i++) {
          params[i] = children[i].eval(interpreter);
        }
        result = ((Function) value).eval(interpreter, params);
      } else if (children.length == 0) {
        result = value;
      } else {
        TreeMap<Integer, Object> arr = (TreeMap<Integer, Object>) value;
        for (int i = 0; i < children.length - 1 && arr != null; i++) {
          arr = (TreeMap<Integer, Object>) arr.get((int) evalDouble(interpreter, i));
        }
        result = arr == null ? null : arr.get((int) evalDouble(interpreter, children.length - 1));
      }
    }
    return result == null ? base.toString().endsWith("$") ? "" : 0.0 : result;
  }

  public Type returnType() {
    return base.toString().endsWith("$") ? Type.STRING : Type.NUMBER;
  }

  public String toString() {
    return base + "(" + super.toString() + ")";
  }
}
