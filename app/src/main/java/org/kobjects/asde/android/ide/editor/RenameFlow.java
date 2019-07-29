package org.kobjects.asde.android.ide.editor;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.lang.StaticSymbol;
import org.kobjects.asde.lang.SymbolOwner;
import org.kobjects.asde.lang.refactor.Rename;

public class RenameFlow {

  public static void start(MainActivity mainActivity, StaticSymbol symbol) {
    new InputFlowBuilder(mainActivity, "Rename", newName -> {
      newName = newName.trim();
      SymbolOwner owner = symbol.getOwner();
      owner.removeSymbol(symbol);
      String oldName = symbol.getName();
      symbol.setName(newName);
      owner.addSymbol(symbol);
      mainActivity.program.accept(new Rename(symbol, oldName, newName));
    }).setLabel("Name")
        .setValue(symbol.getName())
        .setPositiveLabel("Rename")
        .setValidatorFactory(nameInput -> new SymbolNameValidator(symbol.getOwner(), nameInput))
        .start();
  }
}
