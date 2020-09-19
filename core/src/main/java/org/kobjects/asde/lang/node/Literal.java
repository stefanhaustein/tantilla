package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.wasm.Wasm;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;
import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.io.SyntaxColor;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;

public class Literal extends ExpressionNode {
  public final Object value;
  private final Format format;

  public enum Format {
    DEFAULT,
    HEX
  }

  public Literal(Object value, Format format) {
    super((Node[]) null);
    this.value = value;
    this.format = format;
  }

  public Literal(Object value) {
    this(value, Format.DEFAULT);
  }


  @Override
  public Type resolveWasmImpl(WasmExpressionBuilder wasm, ValidationContext resolutionContext, int line) {
    if (value instanceof Boolean) {
      wasm.opCode(value.equals(Boolean.TRUE) ? Wasm.BOOL_TRUE : Wasm.BOOL_FALSE);
    /*
    } else if (value instanceof Number) {
      wasm.opCode(Wasm.F64_CONST);
      wasm.f64(((Number) value).doubleValue()); */
    } else {
      wasm.opCode(Wasm.OBJ_CONST);
      wasm.object(value);
    }
    return Types.of(value);
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    if (value instanceof String) {
      appendLinked(asb, "\"" + ((String) value).replace("\"", "\"\"") + '"', errors, SyntaxColor.STRING);
    } else if (format == Format.HEX && returnType() == Types.FLOAT && ((Number) value).longValue() == ((Number) value).doubleValue()) {
      appendLinked(asb, "0x" + Long.toHexString(((Number) value).longValue()), errors);
    } else {
      appendLinked(asb, Program.toString(value), errors);
    }
  }
}
