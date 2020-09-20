package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.wasm.Wasm;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;
import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.type.Types;

import java.util.Map;

public class BitOperator extends ExpressionNode {
  public enum Kind {
    AND, OR, XOR, SHL, SHR
  }

  private final Kind kind;


  public BitOperator(BitOperator.Kind kind, ExpressionNode child1, ExpressionNode child2) {
    super(child1, child2);
    this.kind = kind;
  }

  String getName() {
    switch (kind) {
      case AND:
        return "&";
      case OR:
        return "|";
      case XOR:
        return "^";
      case SHL:
        return "<<";
      case SHR:
        return ">>";
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  protected Type resolveWasmImpl(WasmExpressionBuilder wasm, ValidationContext resolutionContext, int line) {
    children[0].resolveWasm(wasm, resolutionContext, line, Types.FLOAT);
    wasm.opCode(Wasm.I64_TRUNC_F64_S);
    children[1].resolveWasm(wasm, resolutionContext, line, Types.FLOAT);
    wasm.opCode(Wasm.I64_TRUNC_F64_S);
    switch (kind) {
      case AND:
        wasm.opCode(Wasm.I64_AND);
        break;
      case OR:
        wasm.opCode(Wasm.I64_OR);
        break;
      case XOR:
        wasm.opCode(Wasm.I64_XOR);
        break;
      case SHL:
        wasm.opCode(Wasm.I64_SHL);
        break;
      case SHR:
        wasm.opCode(Wasm.I64_SHR_S);
        break;
      default:
        throw new IllegalStateException();
    }
    wasm.opCode(Wasm.F64_CONVERT_I64_S);
    return Types.FLOAT;
  }


  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    children[0].toString(asb, errors, preferAscii);
    appendLinked(asb, ' ' + getName() + ' ', errors);
    children[1].toString(asb, errors, preferAscii);
  }
}
