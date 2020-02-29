package org.kobjects.asde.android.ide.symbol;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.widget.InputFlowBuilder;
import org.kobjects.asde.lang.classifier.Struct;
import org.kobjects.asde.lang.classifier.GenericProperty;

public class RenameFlow {

  public static void start(MainActivity mainActivity, GenericProperty symbol) {
    new InputFlowBuilder(mainActivity, "Rename '" + symbol.getName() + "'" )
        .addInput("New Name", symbol.getName(), new PropertyNameValidator(symbol.getOwner()))
        .start(result -> {
          String newName = result[0].trim();
          Struct owner = (Struct) symbol.getOwner();
          String oldName = symbol.getName();
          owner.remove(oldName);
          symbol.setName(newName);
          owner.addSymbol(symbol);

          mainActivity.program.processNodes(node -> node.rename(symbol, oldName, newName));
        });
  }
}
