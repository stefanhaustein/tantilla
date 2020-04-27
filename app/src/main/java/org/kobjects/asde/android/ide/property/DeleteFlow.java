package org.kobjects.asde.android.ide.property;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.widget.InputFlowBuilder;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.classifier.clazz.ClassType;

public class DeleteFlow {

  public static void start(final MainActivity mainActivity, final Property symbol) {

    new InputFlowBuilder(mainActivity, "Delete '" + symbol.getName() + "'")
        .setConfirmationCheckbox("Yes, I am sure!")
        .setPositiveLabel("Delete")
        .start(result -> {
          (symbol.getOwner()).remove(symbol.getName());
          mainActivity.program.notifyProgramChanged();
    });
  }


}
