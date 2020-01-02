package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.Types;
import org.kobjects.asde.lang.function.FunctionValidationContext;
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
    return getName(false);
  }

  private String getName(boolean preferAscii) {
    if (preferAscii) {
      switch (val1) {
        case -1:
          return val2 == 0 ? "<=" : val2 == 1 ? "<>" : "<";
        case 0:
          return val2 == 0 ? "=" : val2 == -1 ? "<=" : ">=";
        case 1:
          return val2 == 0 ? ">=" : val2 == -1 ? "<>" : ">";
      }
    } else {
      switch (val1) {
        case -1:
          return val2 == 0 ? "≤" : val2 == 1 ? "≠" : "<";
        case 0:
          return val2 == 0 ? "=" : val2 == -1 ? "≤" : "≥";
        case 1:
          return val2 == 0 ? "≥" : val2 == -1 ? "≠" : ">";
      }
    }
    throw new IllegalStateException();
  }

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
    if (!Types.match(children[0].returnType(), children[1].returnType())) {
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
    int cmp = Integer.signum(((Comparable) lVal).compareTo(rVal));
    return cmp == val1 || cmp == val2;
  }

  @Override
  public Type returnType() {
    return Types.BOOLEAN;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
      children[0].toString(asb, errors, preferAscii);
      appendLinked(asb, ' ' + getName(preferAscii) + ' ', errors);
      children[1].toString(asb, errors, preferAscii);
  }

}
