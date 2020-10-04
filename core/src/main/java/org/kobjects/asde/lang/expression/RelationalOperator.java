package org.kobjects.asde.lang.expression;

import org.kobjects.asde.lang.wasm.Wasm;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;
import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;

public class RelationalOperator extends ExpressionNode {

  public enum Kind {
    EQ, NE, LT, GT, LE, GE
  }

  private final Kind kind;

  public RelationalOperator(Kind kind, ExpressionNode child1, ExpressionNode child2) {
    super(child1, child2);
    this.kind = kind;
  }

  public String getName() {
    return getName(false);
  }

  private String getName(boolean preferAscii) {
      switch (kind) {
        case EQ:
          return "==";
        case NE:
          return preferAscii ? "!=" : "≠";
        case LT:
          return "<";
        case GT:
          return ">";
        case LE:
          return preferAscii ? "<=" : "≤";
        case GE:
          return preferAscii ? ">=" : "≥";
    }
    throw new IllegalStateException();
  }

  @Override
  protected Type resolveWasmImpl(WasmExpressionBuilder wasm, ValidationContext resolutionContext, int line) {
    Type t0 = children[0].resolveWasm(wasm, resolutionContext, line);
    Type t1 = children[1].resolveWasm(wasm, resolutionContext, line);
    if (!t0.equals(t1)) {
      throw new RuntimeException("Argument types must match for relational expressions; got "
              + t0 + " and " + t1);
    }
    if (t0 == Types.FLOAT)
      switch (kind) {
        case EQ:
          wasm.opCode(Wasm.F64_EQ);
          break;
        case NE:
          wasm.opCode(Wasm.F64_NE);
          break;
        case LT:
          wasm.opCode(Wasm.F64_LT);
          break;
        case GT:
          wasm.opCode(Wasm.F64_GT);
          break;
        case LE:
          wasm.opCode(Wasm.F64_LE);
          break;
        case GE:
          wasm.opCode(Wasm.F64_GE);
          break;
    } else {
      switch (kind) {
        case EQ:
          wasm.opCode(Wasm.OBJ_EQ);
          break;
        case NE:
          wasm.opCode(Wasm.OBJ_NE);
          break;
        case LT:
          wasm.opCode(Wasm.OBJ_LT);
          break;
        case GT:
          wasm.opCode(Wasm.OBJ_GT);
          break;
        case LE:
          wasm.opCode(Wasm.OBJ_LE);
          break;
        case GE:
          wasm.opCode(Wasm.OBJ_GE);
          break;
      }
    }
    return Types.BOOL;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
      children[0].toString(asb, errors, preferAscii);
      appendLinked(asb, ' ' + getName(preferAscii) + ' ', errors);
      children[1].toString(asb, errors, preferAscii);
  }

}
