package org.kobjects.asde.android.ide;

import org.kobjects.android64.Android64;
import org.kobjects.asde.lang.event.ProgramListener;
import org.kobjects.asde.lang.type.Types;

public class C64ModeControl {

  private final MainActivity mainActivity;
  private Android64 android64;

  C64ModeControl(MainActivity mainActivity) {
    this.mainActivity = mainActivity;
  }

  void enable() {
    if (android64 != null) {
      return;
    }
    mainActivity.program.legacyMode = true;

    android64 = new Android64(mainActivity.screen);
    mainActivity.program.legacyMode = true;
    mainActivity.program.sendProgramEvent(ProgramListener.Event.MODE_CHANGED);

    mainActivity.program.addBuiltinFunction("peek", (evaluationContext, paramCount) ->
           android64.peek(((Number) evaluationContext.getParameter(0)).intValue()),
        "Returns the memory content at the given address", Types.NUMBER, Types.NUMBER);

    mainActivity.program.addBuiltinFunction("poke", (evaluationContext, paramCount) -> {
          android64.poke(
              ((Number) evaluationContext.getParameter(0)).intValue(),
              ((Number) evaluationContext.getParameter(1)).intValue());
          return null;
        },
        "Returns the memory content at the given address", Types.VOID, Types.NUMBER, Types.NUMBER);

      android64.cls();
  }

  public boolean isEnabled() {
    return android64 != null;
  }

  public void cls() {
    if (android64 != null) {
      android64.cls();
    }
  }
}
