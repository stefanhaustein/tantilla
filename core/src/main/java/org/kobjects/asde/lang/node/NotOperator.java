package org.kobjects.asde.lang.node;

import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;

public class NotOperator extends Node {

  public NotOperator(Node child) {
    super(child);
  }

  @Override
  protected void onResolve(ValidationContext resolutionContext, int line) {
    if (Types.BOOL != children[0].returnType()) {
      throw new RuntimeException("Boolean parameter expected.");
    }
  }

  public Object eval(EvaluationContext evaluationContext) {
    return evalBoolean(evaluationContext);
  }

  public boolean evalBoolean(EvaluationContext evaluationContext) {
    return !children[0].evalBoolean(evaluationContext);
  }

  @Override
  public Type returnType() {
    return children[0].returnType();
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb,"not ", errors);
    children[0].toString(asb, errors, preferAscii);
  }
}
