package org.kobjects.asde.lang.wasm;

public class Wasm {
 // Control Instructions

 public static final byte UNREACHABLE = 0x00;
 public static final byte NOP = 0x01;
 public static final byte BLOCK = 0x02;
 public static final byte LOOP = 0x03;
 public static final byte IF = 0x04;
 public static final byte ELSE = 0x05;
 public static final byte END = 0x0B;
 public static final byte BR = 0x0C;
 public static final byte BR_IF = 0x0D;
 public static final byte BR_TABLE = 0x0E;
 public static final byte RETURN = 0x0f;
 public static final byte CALL = 0x10;
 public static final byte CALL_INDIRECT = 0x11;

 // Parametric

 public static final byte DROP = 0x1A;
 public static final byte SELECT = 0x1B;

 // Variable

 public static final byte LOCAL_GET = 0x20;
 public static final byte LOCAL_SET = 0x21;
 public static final byte LOCAL_TEE = 0x22;
 public static final byte GLOBAL_GET = 0x23;
 public static final byte GLOBAL_SET = 0x24;

 // Memory

 public static final byte I32_LOAD = 0x28;
 public static final byte I64_LOAD = 0x29;
 public static final byte F32_LOAD = 0x2A;
 public static final byte F64_LOAD = 0x2B;
 public static final byte I32_LOAD8_S = 0x2C;
 public static final byte I32_LOAD8_U = 0x2D;
 public static final byte I32_LOAD16_S = 0x2E;
 public static final byte I32_LOAD16_U = 0x2F;
 public static final byte I64_LOAD8_S = 0x30;
 public static final byte I64_LOAD8_U = 0x31;
 public static final byte I64_LOAD16_S = 0x32;
 public static final byte I64_LOAD16_U = 0x33;
 public static final byte I64_LOAD32_S = 0x34;
 public static final byte I64_LOAD32_U = 0x35;
 public static final byte I32_STORE = 0x36;
 public static final byte I64_STORE = 0x37;
 public static final byte F32_STORE = 0x38;
 public static final byte F64_STORE = 0x39;
 public static final byte I32_STORE8 = 0x3A;
 public static final byte I32_STORE16 = 0x3B;
 public static final byte I64_STORE8 = 0x3C;
 public static final byte I64_STORE16 = 0x3D;
 public static final byte I64_STORE32 = 0x3E;
 public static final byte MEMORY_SIZE = 0x3F;
 public static final byte MEMORY_GROW = 0x40;

 // Numeric

 public static final byte I32_CONST = 0x41;
 public static final byte I64_CONST = 0x42;
 public static final byte F32_CONST = 0x43;
 public static final byte F64_CONST = 0x44;

 public static final byte I32_EQZ = 0x45;
 public static final byte I32_EQ = 0x46;
 public static final byte I32_NE = 0x47;
 public static final byte I32_LT_S = 0x48;
 public static final byte I32_LT_U = 0x49;
 public static final byte I32_GT_S = 0x4A;
 public static final byte I32_GT_U = 0x4B;
 public static final byte I32_LE_S = 0x4C;
 public static final byte I32_LE_U = 0x4D;
 public static final byte I32_GE_S = 0x4E;
 public static final byte I32_GE_U = 0x4F;

 public static final byte I64_EQZ = 0x50;
 public static final byte I64_EQ = 0x51;
 public static final byte I64_NE = 0x52;
 public static final byte I64_LT_S = 0x53;
 public static final byte I64_LT_U = 0x54;
 public static final byte I64_GT_S = 0x55;
 public static final byte I64_GT_U = 0x56;
 public static final byte I64_LE_S = 0x57;
 public static final byte I64_LE_U = 0x58;
 public static final byte I64_GE_S = 0x59;
 public static final byte I64_GE_U = 0x5A;

 public static final byte F32_EQ = 0x5B;
 public static final byte F32_NE = 0x5C;
 public static final byte F32_LT = 0x5D;
 public static final byte F32_GT = 0x5E;
 public static final byte F32_LE = 0x5F;
 public static final byte F32_GE = 0x60;

 public static final byte F64_EQ = 0x61;
 public static final byte F64_NE = 0x62;
 public static final byte F64_LT = 0x63;
 public static final byte F64_GT = 0x64;
 public static final byte F64_LE = 0x65;
 public static final byte F64_GE = 0x66;

 public static final byte I32_CLZ = 0x67;
 public static final byte I32_CTZ = 0x68;
 public static final byte I32_POPCNT = 0x69;
 public static final byte I32_ADD = 0x6A;
 public static final byte I32_SUB = 0x6B;
 public static final byte I32_MUL = 0x6C;
 public static final byte I32_DIV_S = 0x6D;
 public static final byte I32_DIV_U = 0x6E;
 public static final byte I32_REM_S = 0x6F;
 public static final byte I32_REM_U = 0x70;
 public static final byte I32_AND = 0x71;
 public static final byte I32_OR = 0x72;
 public static final byte I32_XOR = 0x73;
 public static final byte I32_SHL = 0x74;
 public static final byte I32_SHR_S = 0x75;
 public static final byte I32_SHR_U = 0x76;
 public static final byte I32_ROTL = 0x77;
 public static final byte I32_ROTR = 0x78;

 public static final byte I64_CLZ = 0x79;
 public static final byte I64_CTZ = 0x7A;
 public static final byte I64_POPCNT = 0x7B;
 public static final byte I64_ADD = 0x7C;
 public static final byte I64_SUB = 0x7D;
 public static final byte I64_MUL = 0x7E;
 public static final byte I64_DIV_S = 0x7F;
 public static final byte I64_DIV_U = (byte) 0x80;
 public static final byte I64_REM_S = (byte) 0x81;
 public static final byte I64_REM_U = (byte) 0x82;
 public static final byte I64_AND = (byte) 0x83;
 public static final byte I64_OR = (byte) 0x84;
 public static final byte I64_XOR = (byte) 0x85;
 public static final byte I64_SHL = (byte) 0x86;
 public static final byte I64_SHR_S = (byte) 0x87;
 public static final byte I64_SHR_U = (byte) 0x88;
 public static final byte I64_ROTL = (byte) 0x89;
 public static final byte I64_ROTR = (byte) 0x8A;

 public static final byte F32_ABS = (byte) 0x8B;
 public static final byte F32_NEG = (byte) 0x8C;
 public static final byte F32_CEIL = (byte) 0x8D;
 public static final byte F32_FLOOR = (byte) 0x8E;
 public static final byte F32_TRUNC = (byte) 0x8F;
 public static final byte F32_NEAREST = (byte) 0x90;
 public static final byte F32_SQRT = (byte) 0x91;
 public static final byte F32_ADD = (byte) 0x92;
 public static final byte F32_SUB = (byte) 0x93;
 public static final byte F32_MUL = (byte) 0x94;
 public static final byte F32_DIV = (byte) 0x95;
 public static final byte F32_MIN = (byte) 0x96;
 public static final byte F32_MAX = (byte) 0x97;
 public static final byte F32_COPYSIGN = (byte) 0x98;

 public static final byte F64_ABS = (byte) 0x99;
 public static final byte F64_NEG = (byte) 0x9A;
 public static final byte F64_CEIL = (byte) 0x9B;
 public static final byte F64_FLOOR = (byte) 0x9C;
 public static final byte F64_TRUNC = (byte) 0x9D;
 public static final byte F64_NEAREST = (byte) 0x9E;
 public static final byte F64_SQRT = (byte) 0x9F;
 public static final byte F64_ADD = (byte) 0xA0;
 public static final byte F64_SUB = (byte) 0xA1;
 public static final byte F64_MUL = (byte) 0xA2;
 public static final byte F64_DIV = (byte) 0xA3;
 public static final byte F64_MIN = (byte) 0xA4;
 public static final byte F64_MAX = (byte) 0xA5;
 public static final byte F64_COPYSIGN = (byte) 0xA6;

 public static final byte I32_WRAP_I64 = (byte) 0xA7;
 public static final byte I32_TRUNC_F32_S = (byte) 0xA8;
 public static final byte I32_TRUNC_F32_U = (byte) 0xA9;
 public static final byte I32_TRUNC_F64_S = (byte) 0xAA;
 public static final byte I32_TRUNC_F64_U = (byte) 0xAB;
 public static final byte I64_EXTEND_I32_S = (byte) 0xAC;
 public static final byte I64_EXTEND_I32_U = (byte) 0xAD;
 public static final byte I64_TRUNC_F32_S = (byte) 0xAE;
 public static final byte I64_TRUNC_F32_U = (byte) 0xAF;
 public static final byte I64_TRUNC_F64_S = (byte) 0xB0;
 public static final byte I64_TRUNC_F64_U = (byte) 0xB1;
 public static final byte F32_CONVERT_I32_S = (byte) 0xB2;
 public static final byte F32_CONVERT_I32_U = (byte) 0xB3;
 public static final byte F32_CONVERT_I64_S = (byte) 0xB4;
 public static final byte F32_CONVERT_I64_U = (byte) 0xB5;
 public static final byte F32_DEMOTE_F64 = (byte) 0xB6;
 public static final byte F64_CONVERT_I32_S = (byte) 0xB7;
 public static final byte F64_CONVERT_I32_U = (byte) 0xB8;
 public static final byte F64_CONVERT_I64_S = (byte) 0xB9;
 public static final byte F64_CONVERT_I64_U = (byte) 0xBA;
 public static final byte F64_PROMOTE_F32 = (byte) 0xBB;
 public static final byte I32_REINTERPRET_F32 = (byte) 0xBC;
 public static final byte I64_REINTERPRET_F64 = (byte) 0xBD;
 public static final byte F32_REINTERPRET_I32 = (byte) 0xBE;
 public static final byte F64_REINTERPRET_I64 = (byte) 0xBF;

 // Nonstandard Tantilla extensions

 public static final byte F64_POW = (byte) 0xf0;
 public static final byte F64_MOD = (byte) 0xf1;

 public static final byte OBJ_CONST = (byte) 0xd0;
 public static final byte OBJ_EQ = (byte) 0xd1;
 public static final byte OBJ_NE = (byte) 0xd2;
 public static final byte OBJ_LT = (byte) 0xd3;
 public static final byte OBJ_GT = (byte) 0xd4;
 public static final byte OBJ_LE = (byte) 0xd5;
 public static final byte OBJ_GE = (byte) 0xd6;

 public static final byte BOOL_TRUE = (byte) 0xe0;
 public static final byte BOOL_FALSE = (byte) 0xe1;
 public static final byte BOOL_NOT = (byte) 0xe2;

 public static final byte STR_ADD = (byte) 0xf2;
 public static final byte EVAL = (byte) 0xf3;
}
