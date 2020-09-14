package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.wasm.Wasm;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;
import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;

public final class OrOperator extends WasmNode {

  public OrOperator(Node child1, Node child2) {
    super(child1, child2);
  }

  @Override
  protected Type resolveWasmImpl(WasmExpressionBuilder wasm, ValidationContext resolutionContext, int line) {
    children[0].resolveWasm(wasm, resolutionContext, line, Types.BOOL);
    wasm.opCode(Wasm.IF);
    wasm.opCode(Wasm.BOOL_TRUE);
    wasm.opCode(Wasm.ELSE);
    children[1].resolveWasm(wasm, resolutionContext, line, Types.BOOL);
    wasm.opCode(Wasm.END);
    return Types.BOOL;
  }

  public boolean evalBoolean(EvaluationContext evaluationContext) {
    return children[0].evalBoolean(evaluationContext) || children[1].evalBoolean(evaluationContext);
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    children[0].toString(asb, errors, preferAscii);
    appendLinked(asb, " or ", errors);
    children[1].toString(asb, errors, preferAscii);
  }
}
