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
        case Wasm.UNREACHABLE:
          throw new IllegalStateException("Reached unreachable.");
        case Wasm.NOP:
          break;
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

        case Wasm.I32_ADD:
          context.push(context.popInt() + context.popInt());
          break;
        case Wasm.I32_SUB: {
          int z2 = context.popInt();
          context.push(context.popInt() - z2);
          break;
        }
        case Wasm.I32_MUL:
          context.push(context.popInt() * context.popInt());
          break;
        case Wasm.I32_DIV_S: {
          int z2 = context.popInt();
          context.push(context.popInt() / z2);
          break;
        }
        case Wasm.I32_REM_S: {
          int z2 = context.popInt();
          context.push(context.popInt() % z2);
          break;
        }
        case Wasm.I32_AND:
          context.push(context.popInt() & context.popInt());
          break;
        case Wasm.I32_OR:
          context.push(context.popInt() | context.popInt());
          break;
        case Wasm.I32_XOR:
          context.push(context.popInt() ^ context.popInt());
          break;
        case Wasm.I32_SHL: {
          int z2 = context.popInt();
          context.push(context.popInt() << z2);
          break;
        }
        case Wasm.I32_SHR_S: {
          int z2 = context.popInt();
          context.push(context.popInt() >> z2);
          break;
        }
        case Wasm.I32_SHR_U: {
          int z2 = context.popInt();
          context.push(context.popInt() >>> z2);
          break;
        }

        case Wasm.I64_ADD:
          context.push(context.popLong() + context.popLong());
          break;
        case Wasm.I64_SUB: {
          long z2 = context.popLong();
          context.push(context.popLong() - z2);
          break;
        }
        case Wasm.I64_MUL:
          context.push(context.popLong() * context.popLong());
          break;
        case Wasm.I64_DIV_S: {
          long z2 = context.popLong();
          context.push(context.popLong() / z2);
          break;
        }
        case Wasm.I64_REM_S: {
          long z2 = context.popLong();
          context.push(context.popLong() % z2);
          break;
        }
        case Wasm.I64_AND:
          context.push(context.popLong() & context.popLong());
          break;
        case Wasm.I64_OR:
          context.push(context.popLong() | context.popLong());
          break;
        case Wasm.I64_XOR:
          context.push(context.popLong() ^ context.popLong());
          break;
        case Wasm.I64_SHL: {
          long z2 = context.popLong();
          context.push(context.popLong() << z2);
          break;
        }
        case Wasm.I64_SHR_S: {
          long z2 = context.popLong();
          context.push(context.popLong() >> z2);
          break;
        }
        case Wasm.I64_SHR_U: {
          long z2 = context.popLong();
          context.push(context.popLong() >>> z2);
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

        case Wasm.I32_WRAP_I64:
          context.push((int) context.popLong());
          break;
        case Wasm.I32_TRUNC_F32_S:
          context.push((int) context.popFloat());
          break;
        case Wasm.I32_TRUNC_F64_S:
          context.push((int) context.popDouble());
          break;
        case Wasm.I64_EXTEND_I32_S:
          context.push((long) context.popInt());
          break;
        case Wasm.I64_EXTEND_I32_U:
          context.push(context.popInt() & 0xffffffffL);
          break;
        case Wasm.I64_TRUNC_F32_S:
          context.push((long) context.popFloat());
          break;
        case Wasm.I64_TRUNC_F64_S:
          context.push((long) context.popDouble());
          break;
        case Wasm.F32_CONVERT_I32_S:
          context.push((float) context.popInt());
          break;
        case Wasm.F32_CONVERT_I64_S:
          context.push((float) context.popLong());
          break;
        case Wasm.F32_DEMOTE_F64:
          context.push((float) context.popDouble());
          break;
        case Wasm.F64_CONVERT_I32_S:
          context.push((double) context.popInt());
          break;
        case Wasm.F64_CONVERT_I64_S:
          context.push((double) context.popLong());
          break;
        case Wasm.F64_PROMOTE_F32:
          context.push((double) context.popFloat());
          break;

        case Wasm.I32_REINTERPRET_F32:
          context.push(Float.floatToRawIntBits(context.popFloat()));
          break;
        case Wasm.I64_REINTERPRET_F64:
          context.push(Double.doubleToRawLongBits(context.popFloat()));
          break;
        case Wasm.F32_REINTERPRET_I32:
          context.push(Float.intBitsToFloat(context.popInt()));
          break;
        case Wasm.F64_REINTERPRET_I64:
          context.push(Double.longBitsToDouble(context.popLong()));
          break;

        // Custom opcodes 

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
