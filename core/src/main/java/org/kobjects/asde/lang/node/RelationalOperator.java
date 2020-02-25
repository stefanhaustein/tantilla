package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Type;

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
    return getName(false);
  }

  private String getName(boolean preferAscii) {
    if (preferAscii) {
      switch (val1) {
        case -1:
          return val2 == 0 ? "<=" : val2 == 1 ? "<>" : "<";
        case 0:
          return val2 == 0 ? "==" : val2 == -1 ? "<=" : ">=";
        case 1:
          return val2 == 0 ? ">=" : val2 == -1 ? "<>" : ">";
      }
    } else {
      switch (val1) {
        case -1:
          return val2 == 0 ? "≤" : val2 == 1 ? "≠" : "<";
        case 0:
          return val2 == 0 ? "==" : val2 == -1 ? "≤" : "≥";
        case 1:
          return val2 == 0 ? "≥" : val2 == -1 ? "≠" : ">";
      }
    }
    throw new IllegalStateException();
  }

  @Override
  protected void onResolve(ValidationContext resolutionContext, int line) {
    if (!children[0].returnType().equals(children[1].returnType())) {
      throw new RuntimeException("Argument types must match for relational expressions; got "
              + children[0].returnType() + " and " + children[1].returnType());
    }
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    Object lVal = children[0].eval(evaluationContext);
    Object rVal = children[1].eval(evaluationContext);
    if (lVal.getClass() != rVal.getClass()) {
      throw new RuntimeException("Types (" + lVal.getClass() + " and " + rVal.getClass() + ") don't match for relational operator '" + getName() + "'");
    }
    if (lVal instanceof Comparable) {
      int cmp = Integer.signum(((Comparable) lVal).compareTo(rVal));
      return cmp == val1 || cmp == val2;
    }
    if (val1 == 0 && val2 == 0) {
      return lVal.equals(rVal);
    }
    if (val1 == -1 && val2 == 1) {
      return !lVal.equals(rVal);
    }
    throw new RuntimeException("Can't compare type " + lVal.getClass());
  }

  @Override
  public Type returnType() {
    return Types.BOOL;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
      children[0].toString(asb, errors, preferAscii);
      appendLinked(asb, ' ' + getName(preferAscii) + ' ', errors);
      children[1].toString(asb, errors, preferAscii);
  }

}
