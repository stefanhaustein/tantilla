package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.wasm.Wasm;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;
import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;

public class MathOperator extends ExpressionNode {

  public enum Kind {
    ADD, SUB, MUL, DIV, MOD, POW;
  }

  public final Kind kind;


  public MathOperator(Kind kind, ExpressionNode child1, ExpressionNode child2) {
    super(child1, child2);
    this.kind = kind;
  }

  String getName(boolean preferAscii) {
    switch (kind) {
      case ADD:
        return "+";
      case SUB:
        return preferAscii ? "-" : "−";
      case MUL:
        return preferAscii ? "*" : "×";
      case DIV:
        return "/";
      case POW:
        return "**";
      case MOD:
        return "%";
      default:
        throw new IllegalStateException();
    }
  }


  @Override
  public Type resolveWasmImpl(WasmExpressionBuilder wasm, ValidationContext resolutionContext, int line) {
    Type t0 = children[0].resolveWasmImpl(wasm, resolutionContext, line);
    Type t1 = children[1].resolveWasmImpl(wasm, resolutionContext, line);
    if (t0 != Types.FLOAT) {
      if (kind == Kind.ADD) {
        if (t0 == Types.STR) {
          wasm.opCode(Wasm.STR_ADD);
          return Types.STR;
        }
        throw new RuntimeException("Left parameter type should be String or Number; got: " + t0);
      }
      throw new RuntimeException("Left parameter expected to be a Number but is " + t0);
    }
    if (t1 != Types.FLOAT) {
      throw new RuntimeException("Right parameter expected to be a Number but is " + t1);
    }
    switch (kind) {
      case POW:
        wasm.opCode(Wasm.F64_POW);
        break;
      case ADD:
        wasm.opCode(Wasm.F64_ADD);
        break;
      case SUB:
        wasm.opCode(Wasm.F64_SUB);
        break;
      case DIV:
        wasm.opCode(Wasm.F64_DIV);
        break;
      case MUL:
        wasm.opCode(Wasm.F64_MUL);
        break;
      case MOD:
        wasm.opCode(Wasm.F64_MOD);
        break;
      default:
        throw new IllegalStateException("Unsupported binary operator " + kind);
    }
    return Types.FLOAT;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
      children[0].toString(asb, errors, preferAscii);
      if (kind == Kind.ADD || kind == Kind.SUB) {
        appendLinked(asb, ' ' + getName(preferAscii) + ' ', errors);
      } else {
        appendLinked(asb, getName(preferAscii), errors);
      }
      children[1].toString(asb, errors, preferAscii);

  }
}
