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

        case Wasm.BLOCK:
        case Wasm.LOOP:
        case Wasm.IF:
        case Wasm.ELSE:
        case Wasm.END:
        case Wasm.BR:
        case Wasm.BR_IF:
        case Wasm.BR_TABLE:
        case Wasm.RETURN:
        case Wasm.CALL:
        case Wasm.CALL_INDIRECT:
          throw new UnsupportedOperationException("Control op " + Integer.toHexString(code[pc - 1]));

        case Wasm.DROP:
          context.pop();
          break;

        case Wasm.SELECT:
          if (context.popBoolean()) {
            context.pop();
          } else {
            Object o = context.pop();
            context.pop();
            context.push(o);
          }
          break;

        case Wasm.LOCAL_GET: {
          int value = 0;
          int shift = 0;
          while (true) {
            int b = code[pc++];
            value |= (b & 0x7f) << shift;
            shift += 7;
            if ((0x80 & b) == 0) {
              break;
            }
          }
          context.push(context.getLocal(value));
          break;
        }

        case Wasm.LOCAL_TEE:
          Object o = context.pop();
          context.push(o);
          context.push(o);
          // Fallthrough intended
        case Wasm.LOCAL_SET: {
          int value = 0;
          int shift = 0;
          while (true) {
            int b = code[pc++];
            value |= (b & 0x7f) << shift;
            shift += 7;
            if ((0x80 & b) == 0) {
              break;
            }
          }
          context.setLocal(value, context.pop());
          break;
        }

        case Wasm.GLOBAL_GET:
          throw new UnsupportedOperationException("global.get");
        case Wasm.GLOBAL_SET:
          throw new UnsupportedOperationException("global.set");

        case Wasm.I32_LOAD:
        case Wasm.I64_LOAD:
        case Wasm.F32_LOAD:
        case Wasm.F64_LOAD:
        case Wasm.I32_LOAD8_S:
        case Wasm.I32_LOAD8_U:
        case Wasm.I32_LOAD16_S:
        case Wasm.I32_LOAD16_U:
        case Wasm.I64_LOAD8_S:
        case Wasm.I64_LOAD8_U:
        case Wasm.I64_LOAD16_S:
        case Wasm.I64_LOAD16_U:
        case Wasm.I64_LOAD32_S:
        case Wasm.I64_LOAD32_U:
          throw new UnsupportedOperationException("load " + Integer.toHexString(code[pc - 1]));
        case Wasm.I32_STORE:
        case Wasm.I64_STORE:
        case Wasm.F32_STORE:
        case Wasm.F64_STORE:
        case Wasm.I32_STORE8:
        case Wasm.I32_STORE16:
        case Wasm.I64_STORE8:
        case Wasm.I64_STORE16:
        case Wasm.I64_STORE32:
          throw new UnsupportedOperationException("store " + Integer.toHexString(code[pc - 1]));
        case Wasm.MEMORY_GROW:
          throw new UnsupportedOperationException("memory.grow");
        case Wasm.MEMORY_SIZE:
          throw new UnsupportedOperationException("memory.size");

        case Wasm.I32_CONST: {
          int result = 0;
          int shift = 0;
          while (true) {
            int b = code[pc++];
            result |= (b & 0x7f) << shift;
            shift += 7;
            if ((0x80 & b) == 0) {
              if (shift < 32 && (b & 0x40) != 0) {
                result |= (~0 << shift);
              }
              break;
            }
          }
          context.push(result);
          break;
        }

        case Wasm.I64_CONST: {
          long result = 0;
          int shift = 0;
          while (true) {
            int b = code[pc++];
            result |= (b & 0x7fL) << shift;
            shift += 7;
            if ((0x80 & b) == 0) {
              if (shift < 64 && (b & 0x40) != 0) {
                result |= (~0L << shift);
              }
              break;
            }
          }
          context.push(result);
          break;
        }

        case Wasm.F32_CONST: {
          int i = (code[pc] & 255)
              | ((code[pc + 1] & 255) << 8)
              | ((code[pc + 2] & 255) << 16)
              | ((code[pc + 3] & 255) << 24);
          pc += 4;
          context.push(Float.intBitsToFloat(i));
              break;
        }

        case Wasm.F64_CONST: {
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
        }

        case Wasm.I32_EQZ:
          context.push(context.popInt() == 0);
          break;
        case Wasm.I32_EQ:
          context.push(context.popInt() == context.popInt());
          break;
        case Wasm.I32_LT_S: {
          int z2 = context.popInt();
          context.push(context.popInt() < z2);
          break;
        }
        case Wasm.I32_GT_S: {
          int z2 = context.popInt();
          context.push(context.popInt() > z2);
          break;
        }
        case Wasm.I32_LE_S: {
          int z2 = context.popInt();
          context.push(context.popInt() <= z2);
          break;
        }
        case Wasm.I32_GE_S: {
          int z2 = context.popInt();
          context.push(context.popInt() >= z2);
          break;
        }
        case Wasm.I32_LT_U:
        case Wasm.I32_GT_U:
        case Wasm.I32_LE_U:
        case Wasm.I32_GE_U:
          throw new UnsupportedOperationException("i32 unsigned comparison: " + Integer.toHexString(code[pc - 1]&255));

        case Wasm.I64_EQZ:
          context.push(context.popLong() == 0);
          break;
        case Wasm.I64_EQ:
          context.push(context.popLong() == context.popLong());
          break;
        case Wasm.I64_LT_S: {
          long z2 = context.popLong();
          context.push(context.popLong() < z2);
          break;
        }
        case Wasm.I64_GT_S: {
          long z2 = context.popLong();
          context.push(context.popLong() > z2);
          break;
        }
        case Wasm.I64_LE_S: {
          long z2 = context.popInt();
          context.push(context.popLong() <= z2);
          break;
        }
        case Wasm.I64_GE_S: {
          long z2 = context.popInt();
          context.push(context.popLong() >= z2);
          break;
        }
        case Wasm.I64_LT_U:
        case Wasm.I64_GT_U:
        case Wasm.I64_LE_U:
        case Wasm.I64_GE_U:
          throw new UnsupportedOperationException("i64 unsigned comparison: " + Integer.toHexString(code[pc - 1]&255));

        case Wasm.F32_EQ:
          context.push(context.popDouble() == context.popDouble());
          break;
        case Wasm.F32_NE:
          context.push(context.popDouble() != context.popDouble());
          break;
        case Wasm.F32_LT: {
          float z2 = context.popFloat();
          context.push(context.popFloat() < z2);
          break;
        }
        case Wasm.F32_GT: {
          float z2 = context.popFloat();
          context.push(context.popFloat() > z2);
          break;
        }
        case Wasm.F32_LE: {
          float z2 = context.popFloat();
          context.push(context.popFloat() <= z2);
          break;
        }
        case Wasm.F32_GE: {
          float z2 = context.popFloat();
          context.push(context.popFloat() >= z2);
          break;
        }

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

        case Wasm.I32_CLZ:
          throw new UnsupportedOperationException("i32.clz");
        case Wasm.I32_CTZ:
          throw new UnsupportedOperationException("i32.ctz");
        case Wasm.I32_POPCNT:
          throw new UnsupportedOperationException("i32.popcnt");
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
        case Wasm.I32_DIV_U:
          throw new UnsupportedOperationException("i32.div_u");
        case Wasm.I32_REM_S: {
          int z2 = context.popInt();
          context.push(context.popInt() % z2);
          break;
        }
        case Wasm.I32_REM_U:
          throw new UnsupportedOperationException("i32.div_u");
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
        case Wasm.I32_ROTL:
          throw new UnsupportedOperationException("i32.rotl");
        case Wasm.I32_ROTR:
          throw new UnsupportedOperationException("i32.rotl");

        case Wasm.I64_CLZ:
          throw new UnsupportedOperationException("i64.clz");
        case Wasm.I64_CTZ:
          throw new UnsupportedOperationException("i64.ctz");
        case Wasm.I64_POPCNT:
          throw new UnsupportedOperationException("i64.popcnt");
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
        case Wasm.I64_DIV_U:
          throw new UnsupportedOperationException("i64.div_u");
        case Wasm.I64_REM_S: {
          long z2 = context.popLong();
          context.push(context.popLong() % z2);
          break;
        }
        case Wasm.I64_REM_U:
          throw new UnsupportedOperationException("i64.rem_u");
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
        case Wasm.I64_ROTL:
          throw new UnsupportedOperationException("i64.rotl");
        case Wasm.I64_ROTR:
          throw new UnsupportedOperationException("i64.rotr");

        case Wasm.F32_ABS:
          context.push(Math.abs(context.popFloat()));
          break;
        case Wasm.F32_NEG:
          context.push(-context.popFloat());
          break;
        case Wasm.F32_CEIL:
          context.push(Math.ceil(context.popFloat()));
          break;
        case Wasm.F32_FLOOR:
          context.push(Math.floor(context.popFloat()));
          break;
        case Wasm.F32_TRUNC:
          throw new UnsupportedOperationException("f32.trunc");
        case Wasm.F32_NEAREST:
          throw new UnsupportedOperationException("f32.nearest");
        case Wasm.F32_SQRT:
          context.push((float) Math.sqrt(context.popFloat()));
          break;
        case Wasm.F32_ADD:
          context.push(context.popFloat() + context.popFloat());
          break;
        case Wasm.F32_SUB: {
          float z2 = context.popFloat();
          context.push(context.popFloat() - z2);
          break;
        }
        case Wasm.F32_MUL:
          context.push(context.popFloat() * context.popFloat());
          break;
        case Wasm.F32_DIV:{
          float z2 = context.popFloat();
          context.push(context.popFloat() / z2);
          break;
        }
        case Wasm.F32_MIN:
          context.push(Math.min(context.popFloat(), context.popFloat()));
          break;
        case Wasm.F32_MAX:
          context.push(Math.max(context.popFloat(), context.popFloat()));
          break;
        case Wasm.F32_COPYSIGN: {
          float z2 = context.popFloat();
          context.push(Math.copySign(context.popFloat(), z2));
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
          throw new UnsupportedOperationException("f64.trunc");
        case Wasm.F64_NEAREST:
          throw new UnsupportedOperationException("f64.nearest");
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
        case Wasm.I32_TRUNC_F32_U:
          throw new UnsupportedOperationException("i32.trunc_f32_u");
        case Wasm.I32_TRUNC_F64_S:
          context.push((int) context.popDouble());
          break;
        case Wasm.I32_TRUNC_F64_U:
          throw new UnsupportedOperationException("i32.trunc_f64_u");
        case Wasm.I64_EXTEND_I32_S:
          context.push((long) context.popInt());
          break;
        case Wasm.I64_EXTEND_I32_U:
          context.push(context.popInt() & 0xffffffffL);
          break;
        case Wasm.I64_TRUNC_F32_S:
          context.push((long) context.popFloat());
          break;
        case Wasm.I64_TRUNC_F32_U:
          throw new UnsupportedOperationException("i64.trunc_f32_u");
        case Wasm.I64_TRUNC_F64_S:
          context.push((long) context.popDouble());
          break;
        case Wasm.I64_TRUNC_F64_U:
          throw new UnsupportedOperationException("i64.trunc_f64_u");
        case Wasm.F32_CONVERT_I32_S:
          context.push((float) context.popInt());
          break;
        case Wasm.F32_CONVERT_I32_U:
          throw new UnsupportedOperationException("f32.convert_i32_u");
        case Wasm.F32_CONVERT_I64_S:
          context.push((float) context.popLong());
          break;
        case Wasm.F32_CONVERT_I64_U:
          throw new UnsupportedOperationException("f32.convert_i64_u");
        case Wasm.F32_DEMOTE_F64:
          context.push((float) context.popDouble());
          break;
        case Wasm.F64_CONVERT_I32_S:
          context.push((double) context.popInt());
          break;
        case Wasm.F64_CONVERT_I32_U:
          throw new UnsupportedOperationException("f64.convert_i32_u");
        case Wasm.F64_CONVERT_I64_S:
          context.push((double) context.popLong());
          break;
        case Wasm.F64_CONVERT_I64_U:
          throw new UnsupportedOperationException("f64.convert_i64_u");
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
    return context.pop();
  }

}
