package org.kobjects.asde.android.ide;

import org.kobjects.android64.Android64;
import org.kobjects.asde.lang.event.ProgramListener;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.type.Types;

public class C64ModeControl implements ProgramListener {

  private final MainActivity mainActivity;
  private Android64 android64;

  C64ModeControl(MainActivity mainActivity) {
    this.mainActivity = mainActivity;
    mainActivity.program.addProgramNameChangeListener(this);
  }


  void disable() {
    if (android64 == null) {
      return;
    }
    android64.cls();
    mainActivity.program.removeSymbol(mainActivity.program.getSymbol("peek"));
    mainActivity.program.removeSymbol(mainActivity.program.getSymbol("poke"));
    android64 = null;

    mainActivity.program.sendProgramEvent(ProgramListener.Event.MODE_CHANGED);
    mainActivity.screen.clearAll();
    mainActivity.screen.resetViewport();
  }


  void enable() {
    if (android64 != null) {
      return;
    }
    android64 = new Android64(mainActivity.screen);
    mainActivity.program.setLegacyMode(true);

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

    mainActivity.program.sendProgramEvent(ProgramListener.Event.MODE_CHANGED);
  }


  public boolean isEnabled() {
    return android64 != null;
  }

  public void cls() {
    if (android64 != null) {
      android64.cls();
    }
  }

  boolean checkForPoke() {
    for (Node statement : mainActivity.program.main.allStatements()) {
      if (statement.toString().startsWith("POKE ")) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void programEvent(Event event) {
    switch (event) {
      case LOADED:
        if (mainActivity.program.isLegacyMode()) {
          if (checkForPoke()) {
            enable();
            break;
          }
        }
        disable();
        break;

      case MODE_CHANGED:
        if (!mainActivity.program.isLegacyMode()) {
          disable();
        }
        break;

      case RENAMED:
        // nothing to do here
        break;
    }
  }
}
