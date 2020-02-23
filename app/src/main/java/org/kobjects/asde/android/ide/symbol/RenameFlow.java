package org.kobjects.asde.android.ide.symbol;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.widget.InputFlowBuilder;
import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.classifier.UserClass;
import org.kobjects.asde.lang.classifier.UserProperty;
import org.kobjects.asde.lang.symbol.StaticSymbol;

public class RenameFlow {

  public static void start(MainActivity mainActivity, UserProperty symbol) {
    new InputFlowBuilder(mainActivity, "Rename '" + symbol.getName() + "'" )
        .addInput("New Name", symbol.getName(), new PropertyNameValidator(symbol.getOwner()))
        .start(result -> {
          String newName = result[0].trim();
          UserClass owner = (UserClass) symbol.getOwner();
          String oldName = symbol.getName();
          owner.remove(oldName);
          symbol.setName(newName);
          owner.addSymbol(symbol);

          mainActivity.program.processNodes(node -> node.rename(symbol, oldName, newName));
        });
  }
}
