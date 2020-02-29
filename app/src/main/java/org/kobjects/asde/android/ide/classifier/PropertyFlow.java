package org.kobjects.asde.android.ide.classifier;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.text.ExpressionValidator;
import org.kobjects.asde.android.ide.widget.InputFlowBuilder;
import org.kobjects.asde.android.ide.symbol.PropertyNameValidator;
import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.classifier.GenericProperty;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.program.ProgramListener;

public class PropertyFlow {

  enum Mode {EDIT_INITIALIZER, CREATE_PROPERTY};


  public static void editInitializer(final MainActivity mainActivity, final Property symbol) {
    new PropertyFlow(mainActivity, Mode.EDIT_INITIALIZER, symbol.getOwner(), symbol).showInitializerDialog();
  }

  public static void createProperty(final MainActivity mainActivity, final Classifier owner) {
    new PropertyFlow(mainActivity, Mode.CREATE_PROPERTY, owner, null).showNameDialog();
  }


  private final MainActivity mainActivity;
  private final Mode mode;
  private final Classifier owner;
  private final Property symbol;

  private String name;

  PropertyFlow(MainActivity mainActivity, Mode mode, Classifier owner, Property symbol) {
    this.mainActivity = mainActivity;
    this.mode = mode;
    this.owner = owner;
    this.symbol = symbol;
    name = symbol == null ? "" : symbol.getName();
  }

  private void showNameDialog() {
    new InputFlowBuilder(mainActivity, "Add Property")
        .addInput("Name", name, new PropertyNameValidator(owner))
        .setPositiveLabel("Next")
        .start( result -> {
          this.name = result[0];
          showInitializerDialog();
        });
  }


  private void showInitializerDialog() {
    InputFlowBuilder builder = new InputFlowBuilder(mainActivity, "Property " + name);
    builder.addInput("Initial value", mode == Mode.EDIT_INITIALIZER ? symbol.getInitializer().toString() : null, new ExpressionValidator(mainActivity));
    builder.start(result -> {
      Node parsed =  mainActivity.program.parser.parseExpression(result[0]);
      switch (mode) {
        case CREATE_PROPERTY:
          owner.putProperty(GenericProperty.createWithInitializer(owner, name, parsed));
          mainActivity.program.sendProgramEvent(ProgramListener.Event.CHANGED);
          break;
        case EDIT_INITIALIZER:
          symbol.setInitializer(parsed);
          mainActivity.program.notifySymbolChanged(symbol);
          break;
      }
    });
  }


}
