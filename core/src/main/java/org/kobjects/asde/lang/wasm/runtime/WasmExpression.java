package org.kobjects.asde.lang.wasm.runtime;

import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.runtime.DataArray;
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
    DataArray stack = context.dataStack;
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
          stack.popN(1);
          break;

        case Wasm.SELECT:
          if (!context.popBoolean()) {
            stack.copy(stack.size() - 1, stack.size() - 2);
          }
          stack.popN(1);
          break;

        case Wasm.LOCAL_GET: {
          int index = 0;
          int shift = 0;
          while (true) {
            int b = code[pc++];
            index |= (b & 0x7f) << shift;
            shift += 7;
            if ((0x80 & b) == 0) {
              break;
            }
          }
          stack.copy(context.stackBase + index, stack.push());
          break;
        }

        case Wasm.LOCAL_TEE: {
          int stackTop = stack.size() - 1;
          stack.copy(stackTop, stack.push());
          // Fallthrough intended
        }
        case Wasm.LOCAL_SET: {
          int index = 0;
          int shift = 0;
          while (true) {
            int b = code[pc++];
            index |= (b & 0x7f) << shift;
            shift += 7;
            if ((0x80 & b) == 0) {
              break;
            }
          }
          stack.copy(stack.size() - 1, context.stackBase + index);
          stack.popN(1);
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
          stack.pushI32(result);
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
          stack.pushI64(result);
          break;
        }

        case Wasm.F32_CONST: {
          int i = (code[pc] & 255)
              | ((code[pc + 1] & 255) << 8)
              | ((code[pc + 2] & 255) << 16)
              | ((code[pc + 3] & 255) << 24);
          pc += 4;
          stack.pushF32(Float.intBitsToFloat(i));
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
          stack.pushF64(Double.longBitsToDouble(l));
          break;
        }

        case Wasm.I32_EQZ:
          stack.pushBoolean(stack.popI32() == 0);
          break;
        case Wasm.I32_EQ:
          stack.pushBoolean(stack.popI32() == stack.popI32());
          break;
        case Wasm.I32_LT_S: {
          int z2 = stack.popI32();
          stack.pushBoolean(stack.popI32() < z2);
          break;
        }
        case Wasm.I32_GT_S: {
          int z2 = stack.popI32();
          stack.pushBoolean(stack.popI32() > z2);
          break;
        }
        case Wasm.I32_LE_S: {
          int z2 = stack.popI32();
          stack.pushBoolean(stack.popI32() <= z2);
          break;
        }
        case Wasm.I32_GE_S: {
          int z2 = stack.popI32();
          stack.pushBoolean(stack.popI32() >= z2);
          break;
        }
        case Wasm.I32_LT_U:
        case Wasm.I32_GT_U:
        case Wasm.I32_LE_U:
        case Wasm.I32_GE_U:
          throw new UnsupportedOperationException("i32 unsigned comparison: " + Integer.toHexString(code[pc - 1]&255));

        case Wasm.I64_EQZ:
          stack.pushBoolean(stack.popI64() == 0);
          break;
        case Wasm.I64_EQ:
          stack.pushBoolean(stack.popI64() == stack.popI64());
          break;
        case Wasm.I64_LT_S: {
          long z2 = stack.popI64();
          stack.pushBoolean(stack.popI64() < z2);
          break;
        }
        case Wasm.I64_GT_S: {
          long z2 = stack.popI64();
          stack.pushBoolean(stack.popI64() > z2);
          break;
        }
        case Wasm.I64_LE_S: {
          long z2 = stack.popI32();
          stack.pushBoolean(stack.popI64() <= z2);
          break;
        }
        case Wasm.I64_GE_S: {
          long z2 = stack.popI32();
          stack.pushBoolean(stack.popI64() >= z2);
          break;
        }
        case Wasm.I64_LT_U:
        case Wasm.I64_GT_U:
        case Wasm.I64_LE_U:
        case Wasm.I64_GE_U:
          throw new UnsupportedOperationException("i64 unsigned comparison: " + Integer.toHexString(code[pc - 1]&255));

        case Wasm.F32_EQ:
          stack.pushBoolean(stack.popF32() == stack.popF32());
          break;
        case Wasm.F32_NE:
          stack.pushBoolean(stack.popF32() != stack.popF32());
          break;
        case Wasm.F32_LT: {
          float z2 = stack.popF32();
          stack.pushBoolean(stack.popF32() < z2);
          break;
        }
        case Wasm.F32_GT: {
          float z2 = stack.popF32();
          stack.pushBoolean(stack.popF32() > z2);
          break;
        }
        case Wasm.F32_LE: {
          float z2 = stack.popF32();
          stack.pushBoolean(stack.popF32() <= z2);
          break;
        }
        case Wasm.F32_GE: {
          float z2 = stack.popF32();
          stack.pushBoolean(stack.popF32() >= z2);
          break;
        }

        case Wasm.F64_EQ:
          stack.pushBoolean(stack.popF64() == stack.popF64());
          break;
        case Wasm.F64_NE:
          stack.pushBoolean(stack.popF64() != stack.popF64());
          break;
        case Wasm.F64_LT: {
          double z2 = stack.popF64();
          stack.pushBoolean(stack.popF64() < z2);
          break;
        }
        case Wasm.F64_GT: {
          double z2 = stack.popF64();
          stack.pushBoolean(stack.popF64() > z2);
          break;
        }
        case Wasm.F64_LE: {
          double z2 = stack.popF64();
          stack.pushBoolean(stack.popF64() <= z2);
          break;
        }
        case Wasm.F64_GE: {
          double z2 = stack.popF64();
          stack.pushBoolean(stack.popF64() >= z2);
          break;
        }

        case Wasm.I32_CLZ:
          throw new UnsupportedOperationException("i32.clz");
        case Wasm.I32_CTZ:
          throw new UnsupportedOperationException("i32.ctz");
        case Wasm.I32_POPCNT:
          throw new UnsupportedOperationException("i32.popcnt");
        case Wasm.I32_ADD:
          stack.pushI32(stack.popI32() + stack.popI32());
          break;
        case Wasm.I32_SUB: {
          int z2 = stack.popI32();
          stack.pushI32(stack.popI32() - z2);
          break;
        }
        case Wasm.I32_MUL:
          stack.pushI32(stack.popI32() * stack.popI32());
          break;
        case Wasm.I32_DIV_S: {
          int z2 = stack.popI32();
          stack.pushI32(stack.popI32() / z2);
          break;
        }
        case Wasm.I32_DIV_U:
          throw new UnsupportedOperationException("i32.div_u");
        case Wasm.I32_REM_S: {
          int z2 = stack.popI32();
          stack.pushI32(stack.popI32() % z2);
          break;
        }
        case Wasm.I32_REM_U:
          throw new UnsupportedOperationException("i32.div_u");
        case Wasm.I32_AND:
          stack.pushI32(stack.popI32() & stack.popI32());
          break;
        case Wasm.I32_OR:
          stack.pushI32(stack.popI32() | stack.popI32());
          break;
        case Wasm.I32_XOR:
          stack.pushI32(stack.popI32() ^ stack.popI32());
          break;
        case Wasm.I32_SHL: {
          int z2 = stack.popI32();
          stack.pushI32(stack.popI32() << z2);
          break;
        }
        case Wasm.I32_SHR_S: {
          int z2 = stack.popI32();
          stack.pushI32(stack.popI32() >> z2);
          break;
        }
        case Wasm.I32_SHR_U: {
          int z2 = stack.popI32();
          stack.pushI32(stack.popI32() >>> z2);
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
          stack.pushI64(stack.popI64() + stack.popI64());
          break;
        case Wasm.I64_SUB: {
          long z2 = stack.popI64();
          stack.pushI64(stack.popI64() - z2);
          break;
        }
        case Wasm.I64_MUL:
          stack.pushI64(stack.popI64() * stack.popI64());
          break;
        case Wasm.I64_DIV_S: {
          long z2 = stack.popI64();
          stack.pushI64(stack.popI64() / z2);
          break;
        }
        case Wasm.I64_DIV_U:
          throw new UnsupportedOperationException("i64.div_u");
        case Wasm.I64_REM_S: {
          long z2 = stack.popI64();
          stack.pushI64(stack.popI64() % z2);
          break;
        }
        case Wasm.I64_REM_U:
          throw new UnsupportedOperationException("i64.rem_u");
        case Wasm.I64_AND:
          stack.pushI64(stack.popI64() & stack.popI64());
          break;
        case Wasm.I64_OR:
          stack.pushI64(stack.popI64() | stack.popI64());
          break;
        case Wasm.I64_XOR:
          stack.pushI64(stack.popI64() ^ stack.popI64());
          break;
        case Wasm.I64_SHL: {
          long z2 = stack.popI64();
          stack.pushI64(stack.popI64() << z2);
          break;
        }
        case Wasm.I64_SHR_S: {
          long z2 = stack.popI64();
          stack.pushI64(stack.popI64() >> z2);
          break;
        }
        case Wasm.I64_SHR_U: {
          long z2 = stack.popI64();
          stack.pushI64(stack.popI64() >>> z2);
          break;
        }
        case Wasm.I64_ROTL:
          throw new UnsupportedOperationException("i64.rotl");
        case Wasm.I64_ROTR:
          throw new UnsupportedOperationException("i64.rotr");

        case Wasm.F32_ABS:
          stack.pushF32(Math.abs(stack.popF32()));
          break;
        case Wasm.F32_NEG:
          float value = -stack.popF32();
          stack.pushF32(value);
          break;
        case Wasm.F32_CEIL:
          stack.pushF64(Math.ceil(stack.popF32()));
          break;
        case Wasm.F32_FLOOR:
          stack.pushF64(Math.floor(stack.popF32()));
          break;
        case Wasm.F32_TRUNC:
          throw new UnsupportedOperationException("f32.trunc");
        case Wasm.F32_NEAREST:
          throw new UnsupportedOperationException("f32.nearest");
        case Wasm.F32_SQRT:
          stack.pushF32((float) Math.sqrt(stack.popF32()));
          break;
        case Wasm.F32_ADD:
          stack.pushF32(stack.popF32() + stack.popF32());
          break;
        case Wasm.F32_SUB: {
          float z2 = stack.popF32();
          stack.pushF32(stack.popF32() - z2);
          break;
        }
        case Wasm.F32_MUL:
          stack.pushF32(stack.popF32() * stack.popF32());
          break;
        case Wasm.F32_DIV:{
          float z2 = stack.popF32();
          stack.pushF32(stack.popF32() / z2);
          break;
        }
        case Wasm.F32_MIN:
          stack.pushF32(Math.min(stack.popF32(), stack.popF32()));
          break;
        case Wasm.F32_MAX:
          stack.pushF32(Math.max(stack.popF32(), stack.popF32()));
          break;
        case Wasm.F32_COPYSIGN: {
          float z2 = stack.popF32();
          stack.pushF32(Math.copySign(stack.popF32(), z2));
          break;
        }


        case Wasm.F64_ABS:
          stack.pushF64(Math.abs((Double) stack.popObject()));
          break;
        case Wasm.F64_NEG:
          double value1 = -stack.popF64();
          stack.pushF64(value1);
          break;
        case Wasm.F64_CEIL:
          stack.pushF64(Math.ceil(stack.popF64()));
          break;
        case Wasm.F64_FLOOR:
          stack.pushF64(Math.floor(stack.popF64()));
          break;
        case Wasm.F64_TRUNC:
          throw new UnsupportedOperationException("f64.trunc");
        case Wasm.F64_NEAREST:
          throw new UnsupportedOperationException("f64.nearest");
        case Wasm.F64_SQRT:
          stack.pushF64(Math.sqrt(stack.popF64()));
          break;
        case Wasm.F64_ADD:
          stack.pushF64(stack.popF64() + stack.popF64());
          break;
        case Wasm.F64_SUB: {
          double z2 = stack.popF64();
          stack.pushF64(stack.popF64() - z2);
          break;
        }
        case Wasm.F64_MUL:
          stack.pushF64(stack.popF64() * stack.popF64());
          break;
        case Wasm.F64_DIV:{
          double z2 = stack.popF64();
          stack.pushF64(stack.popF64() / z2);
          break;
        }
        case Wasm.F64_MIN:
          stack.pushF64(Math.min(stack.popF64(), stack.popF64()));
          break;
        case Wasm.F64_MAX:
          stack.pushF64(Math.max(stack.popF64(), stack.popF64()));
          break;
        case Wasm.F64_COPYSIGN: {
          double z2 = stack.popF64();
          stack.pushF64(Math.copySign(stack.popF64(), z2));
          break;
        }

        case Wasm.I32_WRAP_I64:
          stack.pushI32((int) stack.popI64());
          break;
        case Wasm.I32_TRUNC_F32_S:
          stack.pushI32((int) stack.popF32());
          break;
        case Wasm.I32_TRUNC_F32_U:
          throw new UnsupportedOperationException("i32.trunc_f32_u");
        case Wasm.I32_TRUNC_F64_S:
          stack.pushI32((int) stack.popF64());
          break;
        case Wasm.I32_TRUNC_F64_U:
          throw new UnsupportedOperationException("i32.trunc_f64_u");
        case Wasm.I64_EXTEND_I32_S:
          stack.pushI64((long) stack.popI32());
          break;
        case Wasm.I64_EXTEND_I32_U:
          stack.pushI64(stack.popI32() & 0xffffffffL);
          break;
        case Wasm.I64_TRUNC_F32_S:
          stack.pushI64((long) stack.popF32());
          break;
        case Wasm.I64_TRUNC_F32_U:
          throw new UnsupportedOperationException("i64.trunc_f32_u");
        case Wasm.I64_TRUNC_F64_S:
          stack.pushI64((long) stack.popF64());
          break;
        case Wasm.I64_TRUNC_F64_U:
          throw new UnsupportedOperationException("i64.trunc_f64_u");
        case Wasm.F32_CONVERT_I32_S:
          stack.pushF32((float) stack.popI32());
          break;
        case Wasm.F32_CONVERT_I32_U:
          throw new UnsupportedOperationException("f32.convert_i32_u");
        case Wasm.F32_CONVERT_I64_S:
          stack.pushF32((float) stack.popI64());
          break;
        case Wasm.F32_CONVERT_I64_U:
          throw new UnsupportedOperationException("f32.convert_i64_u");
        case Wasm.F32_DEMOTE_F64:
          stack.pushF32((float) stack.popF64());
          break;
        case Wasm.F64_CONVERT_I32_S:
          stack.pushF64((double) stack.popI32());
          break;
        case Wasm.F64_CONVERT_I32_U:
          throw new UnsupportedOperationException("f64.convert_i32_u");
        case Wasm.F64_CONVERT_I64_S:
          stack.pushF64((double) stack.popI64());
          break;
        case Wasm.F64_CONVERT_I64_U:
          throw new UnsupportedOperationException("f64.convert_i64_u");
        case Wasm.F64_PROMOTE_F32:
          stack.pushF64((double) stack.popF32());
          break;

        case Wasm.I32_REINTERPRET_F32:
          stack.pushI32(Float.floatToRawIntBits(stack.popF32()));
          break;
        case Wasm.I64_REINTERPRET_F64:
          stack.pushI64(Double.doubleToRawLongBits(stack.popF32()));
          break;
        case Wasm.F32_REINTERPRET_I32:
          stack.pushF32(Float.intBitsToFloat(stack.popI32()));
          break;
        case Wasm.F64_REINTERPRET_I64:
          stack.pushF64(Double.longBitsToDouble(stack.popI64()));
          break;

        // Custom opcodes 

        case Wasm.F64_POW: {
          double z2 = stack.popF64();
          stack.pushF64(Math.pow(stack.popF64(), z2));
          break;
        }
        case Wasm.F64_MOD: {
          double z2 = stack.popF64();
          stack.pushF64(stack.popF64() % z2);
          break;
        }

        case Wasm.OBJ_CONST: {
          int index = code[pc++] & 255;
          context.push(references[index]);
          break;
        }
        case Wasm.OBJ_EQ:
          stack.pushBoolean(stack.popObject().equals(stack.popObject()));
          break;
        case Wasm.OBJ_NE:
          boolean value2 = !stack.popObject().equals(stack.popObject());
          stack.pushBoolean(value2);
          break;

        case Wasm.OBJ_LT: {
          Comparable z2 = (Comparable) stack.popObject();
          stack.pushBoolean(((Comparable) stack.popObject()).compareTo(z2) < 0);
          break;
        }
        case Wasm.OBJ_GT: {
          Comparable z2 = (Comparable) stack.popObject();
          stack.pushBoolean(((Comparable) stack.popObject()).compareTo(z2) > 0);
          break;
        }
        case Wasm.OBJ_LE: {
          Comparable z2 = (Comparable) stack.popObject();
          stack.pushBoolean(((Comparable) stack.popObject()).compareTo(z2) <= 0);
          break;
        }
        case Wasm.OBJ_GE: {
          Comparable z2 = (Comparable) stack.popObject();
          stack.pushBoolean(((Comparable) stack.popObject()).compareTo(z2) >= 0);
          break;
        }

        case Wasm.BOOL_FALSE:
          stack.pushBoolean(Boolean.FALSE);
          break;
        case Wasm.BOOL_TRUE:
          stack.pushBoolean(Boolean.TRUE);
          break;
        case Wasm.STR_ADD: {
          Object other = stack.popObject();
          context.push(String.valueOf(stack.popObject()) + other);
          break;
        }

        case Wasm.EVAL: {
          int index = code[pc++] & 255;
          Node node = (Node) (references[index]);
          context.push(node.eval(context));
          break;
        }
        case Wasm.CALL_WITH_CONTEXT: {
          CallWithContext callWithContext = (CallWithContext) stack.popObject();
          callWithContext.call(context);
          break;
        }
        default:
          throw new IllegalStateException("Unrecognized opcode " + Integer.toHexString(code[pc-1]&255));
      }
    }
    return stack.popObject();
  }

}
