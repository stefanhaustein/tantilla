package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.asde.lang.node.Node;

import java.util.Map;

public class DebuggerStatement extends Statement {
  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
    // nothing to do here...
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    evaluationContext.control.pause();
    return null;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, "DEBUGGER", errors);
  }
}
