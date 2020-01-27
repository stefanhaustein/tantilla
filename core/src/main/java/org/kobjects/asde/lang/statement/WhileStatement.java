package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.asde.lang.function.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.runtime.EvaluationContext;

import java.util.Map;

public class WhileStatement extends BlockStatement {
  int resolvedStartLine;
  int resolvedEndLine;

  public WhileStatement(Node condition) {
    super(condition);
  }

  @Override
  public void onResolveEnd(FunctionValidationContext resolutionContext, Node parent, int line) {
    resolvedEndLine = line;
  }

  @Override
  void evalEnd(EvaluationContext context) {
    eval(context);
  }

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line) {
    resolvedStartLine = line;
    if (children[0].returnType() != Types.BOOL) {
      throw new RuntimeException("Boolean condition expected.");
    }
    resolutionContext.startBlock(this, line);
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    if (children[0].evalBoolean(evaluationContext)) {
      evaluationContext.currentLine = resolvedStartLine + 1;
    } else {
      evaluationContext.currentLine = resolvedEndLine + 1;
    }
    return null;
  }


  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, "while", errors);
    asb.append(' ');
    children[0].toString(asb, errors, preferAscii);
    asb.append(":");
  }
}
