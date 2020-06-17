package org.kobjects.asde.lang.node;

import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;

public class ImpliedSliceValue extends Node {
  @Override
  protected void onResolve(ValidationContext resolutionContext, int line) {
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    throw new IllegalStateException();
  }

  @Override
  public Type returnType() {
    return Types.FLOAT;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
  }
}
