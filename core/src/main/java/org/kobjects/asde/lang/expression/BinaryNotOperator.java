package org.kobjects.asde.lang.expression;

import org.kobjects.asde.lang.wasm.Wasm;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;
import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.type.Types;

import java.util.Map;

public class BinaryNotOperator extends ExpressionNode {

  public BinaryNotOperator(ExpressionNode child) {
    super(child);
  }

  @Override
  protected Type resolveWasmImpl(WasmExpressionBuilder wasm, ValidationContext resolutionContext, int line) {
    children[0].resolveWasm(wasm, resolutionContext, line, Types.FLOAT);
    wasm.opCode(Wasm.I64_TRUNC_F64_S);
    wasm.opCode(Wasm.I64_CONST);
    wasm.opCode((byte) 0x7f);
    wasm.opCode(Wasm.I64_XOR);
    wasm.opCode(Wasm.F64_CONVERT_I64_S);
    return null;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb,"~", errors);
    children[0].toString(asb, errors, preferAscii);
  }
}
