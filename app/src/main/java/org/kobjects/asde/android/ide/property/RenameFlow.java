package org.kobjects.asde.android.ide.property;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.widget.InputFlowBuilder;
import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.classifier.Property;

public class RenameFlow {

  public static void start(MainActivity mainActivity, Property property) {
    new InputFlowBuilder(mainActivity, "Rename '" + property.getName() + "'" )
        .addInput("New Name", property.getName(), new PropertyNameValidator(property.getOwner()))
        .start(result -> {
          String newName = result[0].trim();
          Classifier owner = property.getOwner();
          owner.remove(property.getName());
          property.setName(newName);
          owner.putProperty(property);

          mainActivity.program.processNodes(node -> node.rename(property));
        });
  }
}
