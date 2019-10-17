package org.kobjects.asde.android.ide.editor;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.lang.ClassImplementation;
import org.kobjects.asde.lang.node.Node;

public class PropertyFlow {

  enum Mode {EDIT_INITIALIZER, CREATE_PROPERTY};


  public static void editInitializer(final MainActivity mainActivity, final ClassImplementation.ClassPropertyDescriptor symbol) {
    new PropertyFlow(mainActivity, Mode.EDIT_INITIALIZER, symbol.getOwner(), symbol).showInitializerDialog();
  }

  public static void createProperty(final MainActivity mainActivity, final ClassImplementation owner) {
    new PropertyFlow(mainActivity, Mode.CREATE_PROPERTY, owner, null).showNameDialog();
  }


  private final MainActivity mainActivity;
  private final Mode mode;
  private final ClassImplementation owner;
  private final ClassImplementation.ClassPropertyDescriptor symbol;

  private String name;

  PropertyFlow(MainActivity mainActivity, Mode mode, ClassImplementation owner, ClassImplementation.ClassPropertyDescriptor symbol) {
    this.mainActivity = mainActivity;
    this.mode = mode;
    this.owner = owner;
    this.symbol = symbol;
    name = symbol == null ? "" : symbol.name();
  }

  private void showNameDialog() {
    new InputFlowBuilder(mainActivity, "Add Property")
        .addInput("Name", name, new SymbolNameValidator(owner))
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
      Node parsed = mainActivity.program.parser.parseExpression(result[0]);
      switch (mode) {
        case CREATE_PROPERTY:
          owner.setProperty(name, parsed);
          mainActivity.program.notifyProgramRenamed();
          break;
        case EDIT_INITIALIZER:
          symbol.setInitializer(parsed);
          mainActivity.program.notifySymbolChanged(symbol);
          break;
      }
    });
  }


}
