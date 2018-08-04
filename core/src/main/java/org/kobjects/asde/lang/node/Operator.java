package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.type.Type;

public class Operator extends Node {
  public final String name;

  public Operator(String name, Node left, Node right) {
    super(left, right);
    this.name = name;
  }

  public Object eval(Interpreter interpreter) {
    Object lVal = children[0].eval(interpreter);
    Object rVal = children[1].eval(interpreter);
    boolean numbers = (lVal instanceof Double) && (rVal instanceof Double);
    if (!numbers) {
      lVal = String.valueOf(lVal);
      rVal = String.valueOf(rVal);
    }
    if ("<=>".indexOf(name.charAt(0)) != -1) {
      int cmp = (((Comparable) lVal).compareTo(rVal));
      return (cmp == 0 ? name.contains("=") : cmp < 0 ? name.contains("<") : name.contains(">"))
          ? -1.0 : 0.0;
    }
    if (!numbers) {
      if (!name.equals("+")) {
        throw new IllegalArgumentException("Numbers arguments expected for operator " + name);
      }
      return "" + lVal + rVal;
    }
    double l = (Double) lVal;
    double r = (Double) rVal;
    switch (name.charAt(0)) {
      case 'a':
        return Double.valueOf(((int) l) & ((int) r));
      case 'o':
        return Double.valueOf(((int) l) | ((int) r));
      case '^':
        return Math.pow(l, r);
      case '+':
        return l + r;
      case '-':
        return l - r;
      case '/':
        return l / r;
      case '*':
        return l * r;
      default:
        throw new RuntimeException("Unsupported operator " + name);
    }
  }

  @Override
  public Type returnType() {
    return (name.equals("+") && (children[0].returnType() == Type.STRING
        || children[1].returnType() == Type.STRING)) ? Type.STRING : Type.NUMBER;
  }

  @Override
  public String toString() {
    return children[0].toString() + ' ' + name + ' ' + children[1].toString();
  }
}
