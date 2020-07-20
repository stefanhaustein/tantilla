package org.kobjects.asde.lang.statement;

import org.kobjects.asde.lang.type.AwaitableType;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.async.Promise;
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
  boolean async;

  public ReturnStatement(Node... children) {
    super(children);
  }

  @Override
  protected void onResolve(ValidationContext resolutionContext, int line) {
    Type returnType = resolutionContext.userFunction.getType().getReturnType();
    async = returnType instanceof AwaitableType;
    Type unwrappedReturnType = async ? ((AwaitableType) returnType).getWrapped() : returnType;

    if (children.length == 0) {
      if (unwrappedReturnType != Types.VOID) {
        throw new RuntimeException("Return value expected for function.");
      }
      resolvedChild = null;
    } else if (children.length == 1) {
      Type type = children[0].returnType();
      if (type instanceof AwaitableType) {
        resolvedChild = TraitCast.autoCast(children[0], returnType, resolutionContext);
      } else {
        resolvedChild = TraitCast.autoCast(children[0], unwrappedReturnType, resolutionContext);
      }
    } else {
      throw new RuntimeException("Impossible");
    }
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    if (resolvedChild != null) {
      Object result = resolvedChild.eval(evaluationContext);
      evaluationContext.returnValue = async ? Promise.of(result) : result;
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
