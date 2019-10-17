package org.kobjects.asde.android.ide.editor;

import android.app.AlertDialog;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.lang.StaticSymbol;

public class DeleteFlow {

  public static void start(final MainActivity mainActivity, final StaticSymbol symbol) {

    new InputFlowBuilder(mainActivity, "Delete '" + symbol.getName() + "'")
        .setMessage("Are you sure?")
        .setPositiveLabel("Delete")
        .start(result -> {
          symbol.getOwner().removeSymbol(symbol);
          mainActivity.program.notifyProgramChanged();
    });
  }


}
