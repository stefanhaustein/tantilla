package org.kobjects.asde.lang.statement;

import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.io.SyntaxColor;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.expression.Node;

import java.util.Map;

public class EndStatement extends Statement {

  BlockStatement resolvedStartStatement;

  @Override
  protected void resolveImpl(ValidationContext resolutionContext, int line) {
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
    appendLinked(asb, "end", errors, SyntaxColor.HIDE);
  }

  @Override
  public boolean closesBlock() {
    return true;
  }
}
