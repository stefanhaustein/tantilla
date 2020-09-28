package org.kobjects.asde.lang.statement;

import org.kobjects.asde.lang.exceptions.ExceptionWithReplacementPropolsal;
import org.kobjects.asde.lang.function.Callable;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.node.ExpressionNode;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.AwaitableType;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;
import org.kobjects.asde.lang.wasm.runtime.WasmExpression;
import org.kobjects.async.Promise;
import org.kobjects.markdown.AnnotatedStringBuilder;

import java.util.Map;

public class LaunchStatement extends Statement {
  private WasmExpression resolvedExpression;

  public LaunchStatement(ExpressionNode expression) {
    super(expression);
  }

  @Override
  protected void resolveImpl(ValidationContext resolutionContext, int line) {
    WasmExpressionBuilder builder = new WasmExpressionBuilder();
    Type returnType = children[0].resolveWasm(builder, resolutionContext, line);
    if (!(returnType instanceof AwaitableType)) {
      throw new RuntimeException("Awaitable expected; got: " + returnType);
    }
    resolvedExpression = builder.build();
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {

    evaluationContext.control.program.getExecutor();

    Promise<?> result = (Promise<?>) resolvedExpression.run(evaluationContext).popObject();

    result.execute(evaluationContext.control.program.getExecutor(), unused -> {});

    return null;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, "launch", errors);
    asb.append(' ');
    children[0].toString(asb, errors, preferAscii);
  }
}
