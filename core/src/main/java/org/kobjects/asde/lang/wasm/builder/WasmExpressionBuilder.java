package org.kobjects.asde.lang.wasm.builder;

import org.kobjects.asde.lang.wasm.Wasm;
import org.kobjects.asde.lang.wasm.runtime.CallWithContext;
import org.kobjects.asde.lang.wasm.runtime.WasmExpression;

import java.util.ArrayList;

public class WasmExpressionBuilder {

  StringBuilder opcodes = new StringBuilder();
  ArrayList<Object> references = new ArrayList<>();

  public WasmExpression build() {
    byte[] code = new byte[opcodes.length()];
    for (int i = 0; i < code.length; i++) {
      code[i] = (byte) opcodes.charAt(i);
    }
    return new WasmExpression(code, references.toArray());
  }

  public WasmExpressionBuilder opCode(byte b) {
    opcodes.append((char) b);
    return this;
  }

  public WasmExpressionBuilder f64(double d) {
    long l = Double.doubleToLongBits(d);
    for (int i = 0; i < 8; i++) {
      opCode((byte) (l & 255));
      l >>>= 8;
    }
    return this;
  }

  public WasmExpressionBuilder object(Object object) {
    opCode((byte) references.size());
    references.add(object);
    return this;
  }

  public WasmExpressionBuilder childBuilder() {
    WasmExpressionBuilder childBuilder = new WasmExpressionBuilder();
    childBuilder.references = this.references;
    return childBuilder;
  }

  public void integrateChildBuilder(WasmExpressionBuilder childBuilder) {
    if (childBuilder.references != references) {
      throw new IllegalArgumentException();
    }
    opcodes.append(childBuilder.opcodes);
  }

  public void objConst(Object object) {
    opCode(Wasm.OBJ_CONST);
    object(object);
  }

  public void callWithContext(CallWithContext call) {
    objConst(call);
    opCode(Wasm.CALL_WITH_CONTEXT);
  }

}
