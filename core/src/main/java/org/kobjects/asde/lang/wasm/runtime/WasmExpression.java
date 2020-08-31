package org.kobjects.asde.lang.wasm.runtime;

import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.wasm.Wasm;

public class WasmExpression {
  private final byte[] code;
  private final Object[] references;

  public WasmExpression(byte[] code, Object[] references) {
    this.code = code;
    this.references = references;
  }

  public Object run(EvaluationContext context) {
    int pc = 0;
    while (pc < code.length) {
      switch (code[pc++]) {
        case Wasm.F64_CONST:
          long l = (code[pc] & 255L)
            | ((code[pc + 1] & 255L) << 8)
            | ((code[pc + 2] & 255L) << 16)
            | ((code[pc + 3] & 255L) << 24)
            | ((code[pc + 4] & 255L) << 32)
            | ((code[pc + 5] & 255L) << 40)
            | ((code[pc + 6] & 255L) << 48)
            | ((code[pc + 7] & 255L) << 56);
          pc += 8;
          context.push(Double.longBitsToDouble(l));
          break;

        case Wasm.F64_EQ:
          context.push(context.popDouble() == context.popDouble());
          break;
        case Wasm.F64_NE:
          context.push(context.popDouble() != context.popDouble());
          break;
        case Wasm.F64_LT: {
          double z2 = context.popDouble();
          context.push(context.popDouble() < z2);
          break;
        }
        case Wasm.F64_GT: {
          double z2 = context.popDouble();
          context.push(context.popDouble() > z2);
          break;
        }
        case Wasm.F64_LE: {
          double z2 = context.popDouble();
          context.push(context.popDouble() <= z2);
          break;
        }
        case Wasm.F64_GE: {
          double z2 = context.popDouble();
          context.push(context.popDouble() >= z2);
          break;
        }

        case Wasm.F64_ABS:
          context.push(Math.abs((Double) context.pop()));
          break;
        case Wasm.F64_NEG:
          context.push(-context.popDouble());
          break;
        case Wasm.F64_CEIL:
          context.push(Math.ceil(context.popDouble()));
          break;
        case Wasm.F64_FLOOR:
          context.push(Math.floor(context.popDouble()));
          break;
        case Wasm.F64_TRUNC:
          throw new UnsupportedOperationException();
        case Wasm.F64_NEAREST:
          throw new UnsupportedOperationException();
        case Wasm.F64_SQRT:
          context.push(Math.sqrt(context.popDouble()));
          break;
        case Wasm.F64_ADD:
          context.push(context.popDouble() + context.popDouble());
          break;
        case Wasm.F64_SUB: {
          double z2 = context.popDouble();
          context.push(context.popDouble() - z2);
          break;
        }
        case Wasm.F64_MUL:
          context.push(context.popDouble() * context.popDouble());
          break;
        case Wasm.F64_DIV:{
          double z2 = context.popDouble();
          context.push(context.popDouble() / z2);
          break;
        }
        case Wasm.F64_MIN:
          context.push(Math.min(context.popDouble(), context.popDouble()));
          break;
        case Wasm.F64_MAX:
          context.push(Math.max(context.popDouble(), context.popDouble()));
          break;
        case Wasm.F64_COPYSIGN: {
          double z2 = context.popDouble();
          context.push(Math.copySign(context.popDouble(), z2));
          break;
        }


        case Wasm.F64_POW: {
          double z2 = context.popDouble();
          context.push(Math.pow(context.popDouble(), z2));
          break;
        }
        case Wasm.F64_MOD: {
          double z2 = context.popDouble();
          context.push(context.popDouble() % z2);
          break;
        }

        case Wasm.OBJ_CONST: {
          int index = code[pc++] & 255;
          context.push(references[index]);
          break;
        }
        case Wasm.OBJ_EQ:
          context.push(context.pop().equals(context.pop()));
          break;
        case Wasm.OBJ_NE:
          context.push(!context.pop().equals(context.pop()));
          break;

        case Wasm.OBJ_LT: {
          Comparable z2 = (Comparable) context.pop();
          context.push(((Comparable) context.pop()).compareTo(z2) < 0);
          break;
        }
        case Wasm.OBJ_GT: {
          Comparable z2 = (Comparable) context.pop();
          context.push(((Comparable) context.pop()).compareTo(z2) > 0);
          break;
        }
        case Wasm.OBJ_LE: {
          Comparable z2 = (Comparable) context.pop();
          context.push(((Comparable) context.pop()).compareTo(z2) <= 0);
          break;
        }
        case Wasm.OBJ_GE: {
          Comparable z2 = (Comparable) context.pop();
          context.push(((Comparable) context.pop()).compareTo(z2) >= 0);
          break;
        }

        case Wasm.BOOL_FALSE:
          context.push(Boolean.FALSE);
          break;
        case Wasm.BOOL_TRUE:
          context.push(Boolean.TRUE);
          break;
        case Wasm.STR_ADD: {
          Object other = context.pop();
          context.push(String.valueOf(context.pop()) + other);
          break;
        }

        case Wasm.EVAL: {
          int index = code[pc++] & 255;
          Node node = (Node) (references[index]);
          context.push(node.eval(context));
          break;
        }
        default:
          throw new IllegalStateException("Unrecognized opcode " + Integer.toHexString(code[pc-1]&255));
      }
    }
    context.popN(1);
    return context.getParameter(0);
  }

}
