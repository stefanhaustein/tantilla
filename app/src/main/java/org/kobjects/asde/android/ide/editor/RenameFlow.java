package org.kobjects.asde.android.ide.editor;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.lang.StaticSymbol;
import org.kobjects.asde.lang.SymbolOwner;
import org.kobjects.asde.lang.refactor.Rename;

public class RenameFlow {

  public static void start(MainActivity mainActivity, StaticSymbol symbol) {
    new InputFlowBuilder(mainActivity, "Rename '" + symbol.getName() + "'" )
        .addInput("New Name", symbol.getName(), new SymbolNameValidator(symbol.getOwner()))
        .start(result -> {
          String newName = result[0].trim();
          SymbolOwner owner = symbol.getOwner();
          owner.removeSymbol(symbol);
          String oldName = symbol.getName();
          symbol.setName(newName);
          owner.addSymbol(symbol);
          mainActivity.program.accept(new Rename(symbol, oldName, newName));
        });
  }
}
