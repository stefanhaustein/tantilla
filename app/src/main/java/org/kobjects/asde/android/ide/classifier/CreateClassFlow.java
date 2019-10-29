package org.kobjects.asde.android.ide.classifier;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.widget.InputFlowBuilder;
import org.kobjects.asde.android.ide.symbol.SymbolNameValidator;
import org.kobjects.asde.lang.ClassImplementation;
import org.kobjects.asde.lang.GlobalSymbol;

public class CreateClassFlow {

  public static void start(MainActivity mainActivity) {
    new InputFlowBuilder(mainActivity, "New Class")
        .addInput("Name", null, new SymbolNameValidator(mainActivity.program))
        .setPositiveLabel("Create")
        .start(result -> {
          ClassImplementation classImplementation = new ClassImplementation(mainActivity.program);
          GlobalSymbol symbol = mainActivity.program.addBuiltin(result[0], classImplementation);
          classImplementation.setDeclaringSymbol(symbol);
        });
  }

}
