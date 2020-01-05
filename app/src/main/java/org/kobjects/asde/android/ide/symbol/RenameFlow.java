package org.kobjects.asde.android.ide.symbol;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.widget.InputFlowBuilder;
import org.kobjects.asde.lang.symbol.StaticSymbol;
import org.kobjects.asde.lang.symbol.SymbolOwner;

public class RenameFlow {

  public static void start(MainActivity mainActivity, StaticSymbol symbol) {
    new InputFlowBuilder(mainActivity, "Rename '" + symbol.getName() + "'" )
        .addInput("Constructor Name", symbol.getName(), new SymbolNameValidator(symbol.getOwner()))
        .start(result -> {
          String newName = result[0].trim();
          SymbolOwner owner = symbol.getOwner();
          owner.removeSymbol(symbol);
          String oldName = symbol.getName();
          symbol.setName(newName);
          owner.addSymbol(symbol);

          mainActivity.program.processNodes(node -> node.rename(symbol, oldName, newName));
        });
  }
}
