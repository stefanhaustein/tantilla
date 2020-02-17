package org.kobjects.asde.android.ide.classifier;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.text.ExpressionValidator;
import org.kobjects.asde.android.ide.widget.InputFlowBuilder;
import org.kobjects.asde.android.ide.symbol.SymbolNameValidator;
import org.kobjects.asde.lang.classifier.UserClass;
import org.kobjects.asde.lang.classifier.UserProperty;
import org.kobjects.asde.lang.program.ProgramListener;
import org.kobjects.asde.lang.statement.DeclarationStatement;

public class PropertyFlow {

  enum Mode {EDIT_INITIALIZER, CREATE_PROPERTY};


  public static void editInitializer(final MainActivity mainActivity, final UserProperty symbol) {
    new PropertyFlow(mainActivity, Mode.EDIT_INITIALIZER, symbol.getOwner(), symbol).showInitializerDialog();
  }

  public static void createProperty(final MainActivity mainActivity, final UserClass owner) {
    new PropertyFlow(mainActivity, Mode.CREATE_PROPERTY, owner, null).showNameDialog();
  }


  private final MainActivity mainActivity;
  private final Mode mode;
  private final UserClass owner;
  private final UserProperty symbol;

  private String name;

  PropertyFlow(MainActivity mainActivity, Mode mode, UserClass owner, UserProperty symbol) {
    this.mainActivity = mainActivity;
    this.mode = mode;
    this.owner = owner;
    this.symbol = symbol;
    name = symbol == null ? "" : symbol.getName();
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
      DeclarationStatement parsed = (DeclarationStatement) mainActivity.program.parser.parseExpression(result[0]);
      switch (mode) {
        case CREATE_PROPERTY:
          owner.setProperty(name, parsed);
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
