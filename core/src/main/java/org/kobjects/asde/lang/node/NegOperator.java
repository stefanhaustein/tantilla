package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.wasm.Wasm;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;
import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;

public class NegOperator extends ExpressionNode {

  public NegOperator(ExpressionNode child) {
    super(child);
  }

  @Override
  protected Type resolveWasmImpl(WasmExpressionBuilder wasm, ValidationContext resolutionContext, int line) {
    Type t0 = children[0].resolveWasm(wasm, resolutionContext, line);
    if (t0 != Types.FLOAT) {
      throw new RuntimeException("Number argument expected for negation.");
    }
    wasm.opCode(Wasm.F64_NEG);
    return Types.FLOAT;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb,"-", errors);
    children[0].toString(asb, errors, preferAscii);
  }
}
