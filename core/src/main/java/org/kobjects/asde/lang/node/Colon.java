package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;

public class Colon extends Node {

  public Colon(Node left, Node right) {
    super(left, right);
  }

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, int line) {
    throw new RuntimeException("colon not permitted in this context.");
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    throw new IllegalStateException();
  }

  @Override
  public Type returnType() {
    return null;
  }
}
