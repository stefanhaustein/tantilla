package org.kobjects.asde.lang.statement;

import org.kobjects.asde.lang.node.ExpressionNode;
import org.kobjects.asde.lang.type.AwaitableType;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;
import org.kobjects.asde.lang.wasm.runtime.WasmExpression;
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

  WasmExpression resolvedExpression;
  boolean async;

  public ReturnStatement(ExpressionNode... children) {
    super(children);
  }

  @Override
  protected void resolveImpl(ValidationContext resolutionContext, int line) {
    Type returnType = resolutionContext.userFunction.getType().getReturnType();
    async = returnType instanceof AwaitableType;
    Type unwrappedReturnType = async ? ((AwaitableType) returnType).getWrapped() : returnType;

    if (children.length == 0) {
      if (unwrappedReturnType != Types.VOID) {
        throw new RuntimeException("Return value expected for function.");
      }
      resolvedExpression = null;
    } else if (children.length == 1) {
      WasmExpressionBuilder builder = new WasmExpressionBuilder();
      Type type = children[0].resolveWasm(builder, resolutionContext, line);
      if (type instanceof AwaitableType) {
        TraitCast.autoCastWasm(builder, type, returnType, resolutionContext);
      } else {
        TraitCast.autoCastWasm(builder, type, unwrappedReturnType, resolutionContext);
      }
      resolvedExpression = builder.build();
    } else {
      throw new RuntimeException("Impossible");
    }
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    if (resolvedExpression != null) {
      Object result = resolvedExpression.run(evaluationContext).popObject();
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
