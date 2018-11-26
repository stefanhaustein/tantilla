package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Builtin;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Types;
import org.kobjects.asde.lang.parser.ResolutionContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class RelationalOperator extends Node {
  public final int val1;
  public final int val2;


  public RelationalOperator(int val1, int val2, Node child1, Node child2) {
    super(child1, child2);
    this.val1 = val1;
    this.val2 = val2;
  }

  public String getName() {
    switch (val1) {
      case -1:
        return val2 == 0 ? "≤" : val2 == 1 ? "≠" : "<";
      case 0:
        return val2 == 0 ? "=" : val2 == -1 ? "≤" : "≥";
      case 1:
        return val2 == 0 ? "≥" : val2 == -1 ? "≠" : ">";
    }
    throw new IllegalStateException();
  }

  @Override
  protected void onResolve(ResolutionContext resolutionContext) {
    if (!Types.match(children[0].returnType(), children[1].returnType())) {
      throw new RuntimeException("Argument types must match for relational expressions; got "
              + children[0].returnType() + " and " + children[1].returnType());
    }
  }

  @Override
  public Object eval(Interpreter interpreter) {
    Object lVal = children[0].eval(interpreter);
    Object rVal = children[1].eval(interpreter);
    if (lVal.getClass() != rVal.getClass()) {
      throw new RuntimeException("Types don't match for relational operator '" + getName() + "'");
    }
    int cmp = (((Comparable) lVal).compareTo(rVal));
    return cmp == val1 || cmp == val2;
  }

  @Override
  public Type returnType() {
    return (children[0].returnType() == null || children[1].returnType() == null)
            ? null : Types.BOOLEAN;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
      children[0].toString(asb, errors);
      appendLinked(asb, ' ' + getName() + ' ', errors);
      children[1].toString(asb, errors);
  }

}
