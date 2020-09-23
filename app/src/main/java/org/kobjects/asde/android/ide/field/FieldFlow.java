package org.kobjects.asde.android.ide.field;

import android.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.text.ExpressionValidator;
import org.kobjects.asde.android.ide.text.TextValidator;
import org.kobjects.asde.android.ide.widget.InputFlowBuilder;
import org.kobjects.asde.android.ide.property.PropertyNameValidator;
import org.kobjects.asde.android.ide.widget.TypeSpinner;
import org.kobjects.asde.lang.classifier.clazz.ClassType;
import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.classifier.clazz.InstanceFieldProperty;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.classifier.StaticProperty;
import org.kobjects.asde.lang.classifier.trait.Trait;
import org.kobjects.asde.lang.node.ExpressionNode;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.program.ProgramListener;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.type.Types;

public class FieldFlow {



  public static void editProperties(final MainActivity mainActivity, final FieldView fieldView) {
    Property property = fieldView.property;
    new FieldFlow(mainActivity, property.getOwner(), fieldView, property.isInstanceField(), property.isMutable()).showInitializerDialog();
  }

  public static void createStaticProperty(final MainActivity mainActivity, final Classifier owner, boolean isMutable) {
    new FieldFlow(mainActivity, owner, null, false, isMutable).showNameDialog();
  }

  public static void createInstanceProperty(final MainActivity mainActivity, final Classifier owner) {
    new FieldFlow(mainActivity, owner, null, true, false).showNameDialog();
  }


  private final MainActivity mainActivity;
  private final Classifier owner;
  private final FieldView fieldView;
  private final boolean isInstanceField;
  private final boolean isMutable;

  private String name;
  private final String title;

  FieldFlow(MainActivity mainActivity, Classifier owner, FieldView fieldView, boolean isInstanceField, boolean isMutable) {
    this.mainActivity = mainActivity;
    this.isInstanceField = isInstanceField;
    this.isMutable = isMutable;
    this.owner = owner;
    this.fieldView = fieldView;
    name = fieldView == null ? "" : fieldView.property.getName();
    title = isInstanceField
        ? ("Property")
        : (isMutable ? "Variable" : "Constant");
  }


  private void showNameDialog() {
    new InputFlowBuilder(mainActivity, "Add " + title)
        .addInput("Name", name, new PropertyNameValidator(owner))
        .setPositiveLabel("Next")
        .start( result -> {
          this.name = result[0];
          showInitializerDialog();
        });
  }


  private void showInitializerDialog() {

    boolean isTrait = owner instanceof Trait;

    LinearLayout inputLayout = new LinearLayout(mainActivity);
    inputLayout.setOrientation(LinearLayout.VERTICAL);


    final EditText editText = new EditText(mainActivity);
    if (fieldView != null && fieldView.property.getInitializer() != null) {
      editText.setText(fieldView.property.getInitializer().toString());
    }

    final TextInputLayout textInputLayout = new TextInputLayout(mainActivity);
    textInputLayout.addView(editText);

    final TextValidator.TextInputLayoutValidator validator = new ExpressionValidator(mainActivity).attach(textInputLayout);

    final TypeSpinner typeSpinner = new TypeSpinner(mainActivity, isTrait ? null  : "Initializer Expression:");
    if (isInstanceField) {
      inputLayout.addView(typeSpinner);
      typeSpinner.setOnItemSelectedListener(
        new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (!isTrait) {
              textInputLayout.setEnabled(position == 0);
              validator.setEnabled(position == 0);
            }
          }

          @Override
          public void onNothingSelected(AdapterView<?> parent) {

          }
        });
      if (fieldView != null && fieldView.property.getInitializer() == null) {
        typeSpinner.selectType(fieldView.property.getType());
        textInputLayout.setEnabled(false);
      }
    } else {
      TextView label = new TextView(mainActivity);
      label.setText("Initializer Expression:");
      inputLayout.addView(label);
    }
    if (!isTrait) {
      inputLayout.addView(textInputLayout);
    }

    CheckBox mutableCheckbox = new CheckBox(mainActivity);
    if (isInstanceField) {
      mutableCheckbox.setText("Mutable");
      inputLayout.addView(mutableCheckbox);
    }

    LinearLayout mainLayout = new LinearLayout(mainActivity);
    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    layoutParams.leftMargin = 56;
    layoutParams.rightMargin = 56;
    layoutParams.topMargin = 28;

    mainLayout.addView(inputLayout, layoutParams);

    AlertDialog.Builder alert = new AlertDialog.Builder(mainActivity);
    alert.setTitle(title + " '" + name + "'");
    alert.setView(mainLayout);

    alert.setNegativeButton("Cancel", null);
    alert.setPositiveButton("Ok", (a, b) -> {
    Type fixedType = typeSpinner.getSelectedType();

    if (fixedType == Types.VOID) {
      fixedType = null;
    }


      ExpressionNode initializer = fixedType == null ? mainActivity.program.parser.parseExpression(editText.getText().toString()) : null;
    //    if (symbol == null) {

      Property property = fixedType == null
          ? (isInstanceField
              ? InstanceFieldProperty.createWithInitializer((ClassType) owner, isMutable, name, initializer)
              : StaticProperty.createWithInitializer(owner, isMutable, name, initializer))
          : InstanceFieldProperty.createUninitialized((ClassType) owner, isMutable, name, fixedType);
      owner.putProperty(property);

      if (fieldView != null) {
        fieldView.property = property;
      }

      mainActivity.program.sendProgramEvent(ProgramListener.Event.CHANGED);
    });

    alert.show();


    /*
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
    }); */
  }


}
