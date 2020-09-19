package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;
import org.kobjects.asde.lang.wasm.runtime.WasmExpression;

public abstract class ExpressionNode extends Node {
  WasmExpression wasmExpression;
  Type resolvedType;

  public ExpressionNode(Node... children) {
    super(children);
  }

  @Override
  public final boolean resolve(ValidationContext resolutionContext, int line) {
    WasmExpressionBuilder wasmExpressionBuilder = new WasmExpressionBuilder();
    resolvedType = resolveWasm(wasmExpressionBuilder, resolutionContext, line);
    wasmExpression = wasmExpressionBuilder.build();
    return resolvedType != null;
  }

  @Override
  protected final void onResolve(ValidationContext resolutionContext, int line) {
    throw new UnsupportedOperationException();
  }

  protected abstract Type resolveWasmImpl(WasmExpressionBuilder wasm, ValidationContext resolutionContext, int line);

  @Override
  public final Object eval(EvaluationContext evaluationContext) {
    try {
      wasmExpression.run(evaluationContext);
      return evaluationContext.dataStack.popObject();
    } catch (Exception e) {
      throw new RuntimeException("Exception in " + toString(), e);
    }
  }

  @Override
  public final Type returnType() {
    return resolvedType;
  }
}
