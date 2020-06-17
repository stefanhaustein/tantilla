package org.kobjects.asde.lang.statement;

import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.io.SyntaxColor;
import org.kobjects.asde.lang.node.TraitCast;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.function.ValidationContext;

import java.util.Map;

public class ReturnStatement extends Statement {

  Node resolvedChild;

  public ReturnStatement(Node... children) {
    super(children);
  }

  @Override
  protected void onResolve(ValidationContext resolutionContext, int line) {
    if (resolutionContext.userFunction.getType().getReturnType() == Types.VOID) {
      if (children.length != 0) {
        throw new RuntimeException("Unexpected return value for subroutine.");
      }
      resolvedChild = null;
    } else {
      if (children.length != 1) {
        throw new RuntimeException("Return value expected for function.");
      }
      resolvedChild = TraitCast.autoCast(children[0], resolutionContext.userFunction.getType().getReturnType(), resolutionContext);
    }
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    if (resolvedChild != null) {
      evaluationContext.returnValue = resolvedChild.eval(evaluationContext);
    }
    evaluationContext.currentLine = Integer.MAX_VALUE;
    return null;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, "return", errors, SyntaxColor.KEYWORD);
    if (children.length > 0) {
      asb.append(' ');
      children[0].toString(asb, errors, preferAscii);
    }
  }
}
