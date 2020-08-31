package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;
import org.kobjects.asde.lang.wasm.runtime.WasmExpression;

public abstract class WasmNode extends Node {
  WasmExpression wasmExpression;
  Type resolvedType;

  public WasmNode(Node... children) {
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
    evaluationContext.ensureExtraStackSpace(100);
    return wasmExpression.run(evaluationContext);
  }

  @Override
  public final Type returnType() {
    return resolvedType;
  }
}
