package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.function.CodeLine;

import java.util.Map;

public class EndStatement extends Statement {

  BlockStatement resolvedStartStatement;

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
    resolvedStartStatement = resolutionContext.endBlock(parent, line, index);
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
}
