package org.kobjects.asde.android.ide.editor;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.lang.ClassImplementation;
import org.kobjects.asde.lang.GlobalSymbol;

public class CreateClassFlow {

  public static void start(MainActivity mainActivity) {
    new InputFlowBuilder(mainActivity, "New Class", className -> {
      ClassImplementation classImplementation = new ClassImplementation(mainActivity.program);
      GlobalSymbol symbol = mainActivity.program.addBuiltin(className, classImplementation);
      classImplementation.setDeclaringSymbol(symbol);
    }).setLabel("Name")
        .setValidatorFactory(input -> new SymbolNameValidator(mainActivity.program, input))
        .setPositiveLabel("Create")
        .start();
  }

}
