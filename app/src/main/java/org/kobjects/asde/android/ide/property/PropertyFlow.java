package org.kobjects.asde.android.ide.property;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.text.ExpressionValidator;
import org.kobjects.asde.android.ide.widget.InputFlowBuilder;
import org.kobjects.asde.android.ide.symbol.PropertyNameValidator;
import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.classifier.Module;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.classifier.GenericProperty;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.program.ProgramListener;

public class PropertyFlow {



  public static void editInitializer(final MainActivity mainActivity, final Property symbol) {
    new PropertyFlow(mainActivity, symbol.getOwner(), symbol, symbol.isInstanceField(), symbol.isMutable()).showInitializerDialog();
  }

  public static void createStaticProperty(final MainActivity mainActivity, final Classifier owner, boolean isMutable) {
    new PropertyFlow(mainActivity, owner, null, false, isMutable).showNameDialog();
  }

  public static void createInstanceProperty(final MainActivity mainActivity, final Classifier owner, boolean isMutable) {
    new PropertyFlow(mainActivity, owner, null, true, isMutable).showNameDialog();
  }


  private final MainActivity mainActivity;
  private final Classifier owner;
  private final Property symbol;
  private final boolean isInstanceField;
  private final boolean isMutable;

  private String name;

  PropertyFlow(MainActivity mainActivity, Classifier owner, Property symbol, boolean isInstanceField, boolean isMutable) {
    this.mainActivity = mainActivity;
    this.isInstanceField = isInstanceField;
    this.isMutable = isMutable;
    this.owner = owner;
    this.symbol = symbol;
    name = symbol == null ? "" : symbol.getName();
  }

  private void showNameDialog() {
    String title = isInstanceField
        ? (isMutable ? "Mutable Property" : "Immutable Property")
        : (isMutable ? "Variable" : "Constant");

    new InputFlowBuilder(mainActivity, "Add " + title)
        .addInput("Name", name, new PropertyNameValidator(owner))
        .setPositiveLabel("Next")
        .start( result -> {
          this.name = result[0];
          showInitializerDialog();
        });
  }


  private void showInitializerDialog() {
    InputFlowBuilder builder = new InputFlowBuilder(mainActivity, "Property " + name);
    builder.addInput("Initial value", symbol != null ? symbol.getInitializer().toString() : null, new ExpressionValidator(mainActivity));
    builder.start(result -> {
      Node parsed =  mainActivity.program.parser.parseExpression(result[0]);
      if (symbol == null) {
        owner.putProperty(GenericProperty.createWithInitializer(owner, isInstanceField, isMutable, name, parsed));
        mainActivity.program.sendProgramEvent(ProgramListener.Event.CHANGED);
      } else {
        symbol.setInitializer(parsed);
        mainActivity.program.notifySymbolChanged(symbol);
      }
    });
  }


}
