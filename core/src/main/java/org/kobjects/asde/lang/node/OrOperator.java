package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;

public final class OrOperator extends Node {

  public OrOperator(Node child1, Node child2) {
    super(child1, child2);
  }

  @Override
  protected void onResolve(ValidationContext resolutionContext, int line) {
    Type t0 = children[0].returnType();
    if (t0 != Types.BOOL) {
      throw new IllegalArgumentException("First argument must be boolean instead of " + t0);
    }
    Type t1 = children[1].returnType();
    if (t1 != Types.BOOL) {
      throw new IllegalArgumentException("Second argument must be boolean instead of " + t1);
    }
  }

  public Object eval(EvaluationContext evaluationContext) {
    return evalBoolean(evaluationContext);
  }

  public boolean evalBoolean(EvaluationContext evaluationContext) {
    return children[0].evalBoolean(evaluationContext) || children[1].evalBoolean(evaluationContext);
  }

  @Override
  public Type returnType() {
    return Types.BOOL;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    children[0].toString(asb, errors, preferAscii);
    appendLinked(asb, " or ", errors);
    children[1].toString(asb, errors, preferAscii);
  }
}
