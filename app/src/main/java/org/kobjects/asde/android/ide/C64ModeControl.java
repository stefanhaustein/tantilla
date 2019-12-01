package org.kobjects.asde.android.ide;

import org.kobjects.android64.Android64;
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

    android64 = new Android64(mainActivity.screen);
    mainActivity.program.legacyMode = true;
    mainActivity.program.notifyProgramNameChanged();

    mainActivity.program.addBuiltinFunction("peek", (evaluationContext, paramCount) ->
           android64.peek(((Number) evaluationContext.getParameter(0)).intValue()),
        "Returns the memory content at the given address", Types.NUMBER, Types.NUMBER);

    mainActivity.program.addBuiltinFunction("poke", (evaluationContext, paramCount) -> {
          android64.poke(
              ((Number) evaluationContext.getParameter(0)).intValue(),
              ((Number) evaluationContext.getParameter(0)).intValue());
          return null;
        },
        "Returns the memory content at the given address", Types.VOID, Types.NUMBER, Types.NUMBER);

  }

  public boolean isEnabled() {
    return android64 != null;
  }
}
