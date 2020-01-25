package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.asde.lang.function.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.runtime.EvaluationContext;

import java.util.Map;

public class WhileStatement extends BlockStatement {
  int resolvedStartLine;
  int resolvedStartIndex;
  int resolvedEndIndex;
  int resolvedEndLine;

  public WhileStatement(Node condition) {
    super(condition);
  }

  @Override
  public void onResolveEnd(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
    resolvedEndLine = line;
    resolvedEndIndex = index;
  }

  @Override
  void evalEnd(EvaluationContext context) {
    eval(context);
  }

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
    resolvedStartLine = line;
    resolvedStartIndex = index;
    if (children[0].returnType() != Types.BOOLEAN) {
      throw new RuntimeException("Boolean condition expected.");
    }
    resolutionContext.startBlock(this, line, index);
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    if (children[0].evalBoolean(evaluationContext)) {
      evaluationContext.currentLine = resolvedStartLine;
      evaluationContext.currentIndex = resolvedStartIndex + 1;
    } else {
      evaluationContext.currentLine = resolvedEndLine;
      evaluationContext.currentIndex = resolvedEndIndex + 1;
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
