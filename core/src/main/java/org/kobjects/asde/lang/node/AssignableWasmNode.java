package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;

public abstract class AssignableWasmNode extends WasmNode {

  public abstract Type resolveForAssignment(WasmExpressionBuilder wasm, ValidationContext validationContext, int line);

}
