package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.wasm.Wasm;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;
import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;

public class AndOperator extends WasmNode {

  public AndOperator(Node child1, Node child2) {
    super(child1, child2);
  }

  @Override
  protected Type resolveWasmImpl(WasmExpressionBuilder wasm, ValidationContext resolutionContext, int line) {
    children[0].resolveWasm(wasm, resolutionContext, line, Types.BOOL);
    wasm.opCode(Wasm.IF);
    children[1].resolveWasm(wasm, resolutionContext, line, Types.BOOL);
    wasm.opCode(Wasm.ELSE);
    wasm.opCode(Wasm.BOOL_FALSE);
    wasm.opCode(Wasm.END);
    return Types.BOOL;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    children[0].toString(asb, errors, preferAscii);
    appendLinked(asb, " AND ", errors);
    children[1].toString(asb, errors, preferAscii);
  }
}
