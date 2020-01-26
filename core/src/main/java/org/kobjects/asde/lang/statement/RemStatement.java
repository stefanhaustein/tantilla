package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.function.FunctionValidationContext;

import java.util.Map;

public class RemStatement extends Statement {

  private final String comment;

  public RemStatement(String comment) {
    this.comment = comment;
  }

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line) {
    // Nothing to do here.
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    return null;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, "REM ", errors);
    asb.append(comment);
  }
}
