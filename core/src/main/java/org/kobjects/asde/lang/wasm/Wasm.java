package org.kobjects.asde.lang.wasm;

public class Wasm {
 public static final byte F64_CONST = 0x44;

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

 public static final byte BOOL_TRUE = (byte) 0xfb;
 public static final byte BOOL_FALSE = (byte) 0xfc;
 public static final byte STR_ADD = (byte) 0xfd;
 public static final byte EVAL = (byte) 0xfe;
 public static final byte OBJECT = (byte) 0xff;
}
