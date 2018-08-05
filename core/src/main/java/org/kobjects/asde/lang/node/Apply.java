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

  public Apply(Program program, Node... children) {
    super(children);
    this.program = program;
  }

  public void set(Interpreter interpreter, Object value) {
    if (!(children[0] instanceof Identifier)) {
        throw new RuntimeException("Not assignable");
    }
    String name = ((Identifier) children[0]).name;

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
    for (int i = 1; i < children.length - 1; i++) {
      int index = evalInt(interpreter, i);
      TreeMap<Integer, Object> sub = (TreeMap<Integer, Object>) target.get(index);
      if (sub == null) {
        sub = new TreeMap<>();
        target.put(index, sub);
      }
      target = sub;
    }
    target.put(evalInt(interpreter, children.length - 1), value);
  }

  public Object eval(Interpreter interpreter) {
    Object value = children[0].evalRaw(interpreter);
    Object result;
    if (value == null) {
      result = null;
    } else {
      if (value instanceof Function) {
        Object[] params = new Object[children.length - 1];
        for (int i = 0; i < params.length; i++) {
          params[i] = children[i + 1].eval(interpreter);
        }
        result = ((Function) value).eval(interpreter, params);
      } else if (children.length == 1) {
        result = value;
      } else {
        TreeMap<Integer, Object> arr = (TreeMap<Integer, Object>) value;
        for (int i = 1; i < children.length - 1 && arr != null; i++) {
          arr = (TreeMap<Integer, Object>) arr.get(evalInt(interpreter, i));
        }
        result = arr == null ? null : arr.get((int) evalInt(interpreter, children.length - 1));
      }
    }
    return result == null ? children[0].toString().endsWith("$") ? "" : 0.0 : result;
  }

  public Type returnType() {
    return children[0].toString().endsWith("$") ? Type.STRING : Type.NUMBER;
  }

  public String toString() {

      StringBuilder sb = new StringBuilder();
      sb.append(children[0].toString());
      sb.append('(');
      for (int i = 1; i < children.length; i++) {
          if (i > 1) {
              sb.append(", ");
          }
          sb.append(children[i].toString());
      }
      sb.append(')');
      return sb.toString();
  }
}
