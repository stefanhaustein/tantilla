package org.kobjects.asde.lang.io;

public class SyntaxColor {

  public static final SyntaxColor KEYWORD = new SyntaxColor(0xffef9108);
  public static final SyntaxColor STRING = new SyntaxColor(0xff79b358);
  public static final SyntaxColor HIDE = new SyntaxColor(0x44ffffff);

  public final int argb;

  SyntaxColor(int argb) {
    this.argb = argb;
  }
}
