package org.kobjects.asde.lang.wasm;

public class Wasm {
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
