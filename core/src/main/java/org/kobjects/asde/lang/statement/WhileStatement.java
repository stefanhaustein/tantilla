package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.function.PropertyValidationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.io.SyntaxColor;
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
  public void onResolveEnd(PropertyValidationContext resolutionContext, EndStatement endStatement, int endLine) {
    resolvedEndLine = endLine;
  }

  @Override
  void evalEnd(EvaluationContext context) {
    eval(context);
  }

  @Override
  protected void onResolve(PropertyValidationContext resolutionContext, int line) {
    resolvedStartLine = line;
    if (children[0].returnType() != Types.BOOL) {
      throw new RuntimeException("Boolean condition expected.");
    }
    resolutionContext.startBlock(this);
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
    appendLinked(asb, "while", errors, SyntaxColor.KEYWORD);
    asb.append(' ');
    children[0].toString(asb, errors, preferAscii);
    asb.append(":");
  }
}
