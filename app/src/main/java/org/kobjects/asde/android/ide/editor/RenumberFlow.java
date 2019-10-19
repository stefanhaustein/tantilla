package org.kobjects.asde.android.ide.editor;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.lang.FunctionImplementation;
import org.kobjects.asde.lang.StaticSymbol;

public class RenumberFlow {

  public static void start(MainActivity mainActivity, StaticSymbol symbol, int firstLine, int lastLine) {

    new InputFlowBuilder(mainActivity,"Renumber")
        .setMessage("Lines " + firstLine + " - " + lastLine)
        .setPositiveLabel("Renumber")
        .addInput("First Line", firstLine, null)
        .addInput("Step", 10, null)
        .start(result -> {
          ((FunctionImplementation) symbol.getValue()).renumber(
              firstLine,
              lastLine,
              Integer.parseInt(result[0]),
              Integer.parseInt(result[1]));
            }
        );


  }

}
