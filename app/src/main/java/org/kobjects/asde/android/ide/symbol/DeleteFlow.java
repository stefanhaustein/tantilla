package org.kobjects.asde.android.ide.symbol;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.widget.InputFlowBuilder;
import org.kobjects.asde.lang.classifier.UserClass;
import org.kobjects.asde.lang.symbol.StaticSymbol;

public class DeleteFlow {

  public static void start(final MainActivity mainActivity, final StaticSymbol symbol) {

    new InputFlowBuilder(mainActivity, "Delete '" + symbol.getName() + "'")
        .setConfirmationCheckbox("Yes, I am sure!")
        .setPositiveLabel("Delete")
        .start(result -> {
          ((UserClass) symbol.getOwner()).remove(symbol.getName());
          mainActivity.program.notifyProgramChanged();
    });
  }


}
