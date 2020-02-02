package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.asde.lang.node.Node;

import java.util.Map;

public class EndStatement extends Statement {

  BlockStatement resolvedStartStatement;

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, int line) {
    resolvedStartStatement = resolutionContext.endBlock();
    resolvedStartStatement.onResolveEnd(resolutionContext, this, line);
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    resolvedStartStatement.evalEnd(evaluationContext);
    return null;
  }


  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, "end", errors);
  }

  @Override
  public boolean closesBlock() {
    return true;
  }
}
