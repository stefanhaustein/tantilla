package org.kobjects.asde.android.ide.symbol;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.widget.InputFlowBuilder;
import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.classifier.Struct;
import org.kobjects.asde.lang.classifier.GenericProperty;

public class RenameFlow {

  public static void start(MainActivity mainActivity, Property symbol) {
    new InputFlowBuilder(mainActivity, "Rename '" + symbol.getName() + "'" )
        .addInput("New Name", symbol.getName(), new PropertyNameValidator(symbol.getOwner()))
        .start(result -> {
          String newName = result[0].trim();
          Classifier owner = symbol.getOwner();
          String oldName = symbol.getName();
          owner.remove(symbol);
          symbol.setName(newName);
          owner.putProperty(symbol);

          mainActivity.program.processNodes(node -> node.rename(symbol, oldName, newName));
        });
  }
}
