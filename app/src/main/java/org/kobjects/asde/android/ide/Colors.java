package org.kobjects.asde.android.ide;

import androidx.core.content.ContextCompat;

import org.kobjects.asde.lang.Program;

public class Colors {

 //   final int primaryMedium;
//    public final int primaryLight;
  //  public final int accentLight;

    public static final int RED = 0xffee4444;
  public static final int BLUE = 0xff4444ee;
    public static final int CYAN = 0xff66ffff;
    public static final int PURPLE = 0xffee66ee;
    public static final int GREEN = 0xff44ee44;
    public static final int YELLOW = 0xffcc8877;
    public static final int ORANGE = 0xffeeaa44;
  public static final int BLACK = 0xff000000;

  public static final int PRIMARY_FILTER = 0x44ffffff;

  public static final int PRIMARY_LIGHT_FILTER = 0x22ffffff;

  public static final int ACCENT = ORANGE;


  public static final int getBackgroundColor(Program program) {
    return program.legacyMode ? BLUE : BLACK;
  }






}
