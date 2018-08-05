package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.type.Type;

public abstract class Node {
  public Node[] children;

  protected Node(Node... children) {
    this.children = children;
  }

  public abstract Object eval(Interpreter interpreter);

  public Object evalRaw(Interpreter interpreter) {
    return eval(interpreter);
  }

  double evalDouble(Interpreter interpreter, int i) {
    Object o = children[i].eval(interpreter);
    if (!(o instanceof Number)) {
      throw new RuntimeException("Number expected in " + this.toString());
    }
    return ((Number) o).doubleValue();
  }

  int evalInt(Interpreter interpreter, int i) {
    return (int) evalDouble(interpreter, i);
  }

  String evalString(Interpreter interpreter, int i) {
    return Program.toString(children[i].eval(interpreter));
  }

  public String toString() {
    if (children.length == 0) {
      return "";
    } else if (children.length == 1) {
      return children[0].toString();
    } else {
      StringBuilder sb = new StringBuilder(children[0].toString());
      for (int i = 1; i < children.length; i++) {
        sb.append(", ");
        sb.append(children[i]);
      }
      return sb.toString();
    }
  }

  public abstract Type returnType();
}
