package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;
import org.kobjects.asde.lang.wasm.runtime.WasmExpression;

public abstract class ExpressionNode extends Node {

  public ExpressionNode(ExpressionNode... children) {
    super(children);
  }

  public final Type resolveWasm(WasmExpressionBuilder wasm, ValidationContext resolutionContext, int line) {
    try {
      return resolveWasmImpl(wasm, resolutionContext, line);
    } catch (Exception e) {
      resolutionContext.addError(this, e);
      return null;
    }
  }

  public final void resolveWasm(WasmExpressionBuilder wasm, ValidationContext resolutionContext, int line, Type expectedType) {
    try {
      Type type = resolveWasmImpl(wasm, resolutionContext, line);
      if (type != expectedType) {
        throw new RuntimeException("Actual type (" + type + ") does not match expected type (" + expectedType + ").");
      }
    } catch (Exception e) {
      resolutionContext.addError(this, e);
    }
  }

  protected abstract Type resolveWasmImpl(WasmExpressionBuilder wasm, ValidationContext resolutionContext, int line);
}
