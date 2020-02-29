package org.kobjects.asde.android.ide.classifier;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.widget.InputFlowBuilder;
import org.kobjects.asde.android.ide.symbol.PropertyNameValidator;
import org.kobjects.asde.lang.classifier.Struct;

public class CreateClassFlow {

  public static void start(MainActivity mainActivity) {
    new InputFlowBuilder(mainActivity, "New Class")
        .addInput("Name", null, new PropertyNameValidator(mainActivity.program.mainModule))
        .setPositiveLabel("Create")
        .start(result -> {
          Struct classImplementation = new Struct(mainActivity.program);
          mainActivity.program.setDeclaration(result[0], classImplementation);
        });
  }

}
