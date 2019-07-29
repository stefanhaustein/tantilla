package org.kobjects.asde.android.ide.editor;

import android.app.AlertDialog;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.lang.StaticSymbol;

public class DeleteFlow {

  public static void start(final MainActivity mainActivity, final StaticSymbol symbol) {
    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mainActivity);
    alertBuilder.setTitle("Confirm Delete");
    alertBuilder.setMessage("Delete symbol '" + symbol.getName() + "'?");
    alertBuilder.setNegativeButton("Cancel", null);
    alertBuilder.setPositiveButton("Delete", (a,b) -> {
      symbol.getOwner().removeSymbol(symbol);
      mainActivity.program.notifyProgramChanged();
    });

    alertBuilder.show();
  }


}
