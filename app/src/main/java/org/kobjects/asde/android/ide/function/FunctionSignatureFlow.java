package org.kobjects.asde.android.ide.function;

import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;

import org.kobjects.asde.R;
import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.property.PropertyNameValidator;
import org.kobjects.asde.android.ide.widget.TypeSpinner;
import org.kobjects.asde.android.ide.widget.IconButton;
import org.kobjects.asde.android.ide.text.TextValidator;
import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.classifier.StaticProperty;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.classifier.trait.Trait;
import org.kobjects.asde.lang.classifier.trait.TraitProperty;
import org.kobjects.asde.lang.function.Parameter;
import org.kobjects.asde.lang.function.UserFunction;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.statement.RemStatement;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.type.Type;

import java.util.ArrayList;
import java.util.HashMap;

public class FunctionSignatureFlow {

  public enum Mode {
    CREATE_GLOBAL, CHANGE_SIGNATURE, CREATE_MEMBER
  }

  final MainActivity mainActivity;
  private final Mode mode;
  Property property;
  String name;
  ArrayList<ParameterWithOriginalIndex> parameterList = new ArrayList<>();
  LinearLayout parameterListView;
  Classifier classifier;
  Type returnType;

  public static void changeSignature(MainActivity mainActivity, Property symbol) {
    FunctionSignatureFlow flow = new FunctionSignatureFlow(mainActivity, Mode.CHANGE_SIGNATURE, ((FunctionType) symbol.getType()).getReturnType());
    flow.property = symbol;
    flow.name = symbol.getName();
    flow.classifier = symbol.getOwner();
    FunctionType functionType = (FunctionType) symbol.getType();
    for (int i = 0; i < functionType.getParameterCount(); i++) {
      Parameter parameter = functionType.getParameter(i);
      flow.parameterList.add(new ParameterWithOriginalIndex(parameter, i));
    }
    flow.editFunctionParameters();
  }

  public static void createMethod(MainActivity mainActivity, Classifier classifier) {
    FunctionSignatureFlow flow = new FunctionSignatureFlow(mainActivity, Mode.CREATE_MEMBER, Types.VOID);
    flow.classifier = classifier;
    flow.createCallableUnit();
  }

  public static void createFunction(MainActivity mainActivity) {
    new FunctionSignatureFlow(mainActivity, Mode.CREATE_GLOBAL, Types.VOID).createCallableUnit();
  }

  private FunctionSignatureFlow(MainActivity mainActivity, Mode mode, Type returnType) {
    this.mainActivity = mainActivity;
    this.mode = mode;
    this.returnType = returnType;
  }


  void createCallableUnit() {
    LinearLayout mainView = new LinearLayout(mainActivity);
    mainView.setOrientation(LinearLayout.VERTICAL);
    TextView nameLabel = new TextView(mainActivity);
    nameLabel.setText("Name");
    mainView.addView(nameLabel);

    TextInputLayout nameInput = new TextInputLayout(mainActivity);
    nameInput.addView(new EditText(mainActivity));
    nameInput.setErrorEnabled(true);
    nameInput.getEditText().setText(name);
    mainView.addView(nameInput);

    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mainActivity);

    alertBuilder.setTitle(mode == Mode.CREATE_MEMBER ? "New Method" : "New Function");
    alertBuilder.setView(mainView);

    alertBuilder.setNegativeButton("Cancel", null);

    alertBuilder.setPositiveButton("Next", (a, b) -> {
      name = nameInput.getEditText().getText().toString();
      editFunctionParameters();
    });
    AlertDialog alert = alertBuilder.show();



    new PropertyNameValidator(
            mode == Mode.CREATE_MEMBER ? classifier : mainActivity.program.mainModule) {
      @Override
      public String validate(String text) {
        String result = super.validate(text);
        alert.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(result == null);
        return result;
      }
    }.attach(nameInput);
  }

  private void updateParameterList() {
    parameterListView.removeAllViews();
    int index = 0;
    for (ParameterWithOriginalIndex parameter : parameterList) {
      final int finalIndex = index;
      LinearLayout parameterView = new LinearLayout(mainActivity);

      IconButton deleteButton = new IconButton(mainActivity, R.drawable.baseline_clear_24);
      parameterView.addView(deleteButton);
      deleteButton.setOnClickListener(event -> {
        parameterList.remove(parameter);
        updateParameterList();
      });

      TextView textView = new TextView(mainActivity);
      textView.setText(parameter.name + ": " + parameter.type);
      LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
      textParams.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
      textView.setOnClickListener(event -> editParameter(finalIndex, false));
      parameterView.addView(textView, textParams);

      IconButton upButton = new IconButton(mainActivity, R.drawable.baseline_arrow_upward_24);
      parameterView.addView(upButton);
      if (index == 0) {
        upButton.setEnabled(false);
      } else {
        upButton.setOnClickListener(event -> {
          parameterList.remove(finalIndex);
          parameterList.add(finalIndex - 1, parameter);
          updateParameterList();
        });
      }

      IconButton downButton = new IconButton(mainActivity, R.drawable.baseline_arrow_downward_24);
      parameterView.addView(downButton);
      if (index == parameterList.size() - 1) {
        downButton.setEnabled(false);
      } else {
        downButton.setOnClickListener(event -> {
          parameterList.remove(parameter);
          parameterList.add(finalIndex + 1, parameter);
          updateParameterList();
        });
      }

      parameterListView.addView(parameterView,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
      index++;
    }
    LinearLayout addParameterView = new LinearLayout(mainActivity);
    IconButton addButton = new IconButton(mainActivity, R.drawable.baseline_add_24);
    addButton.setOnClickListener(event -> {
      editParameter(parameterList.size(), true);});
    addParameterView.addView(addButton);
    parameterListView.addView(addParameterView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    parameterListView.requestLayout();
    parameterListView.invalidate();
  }


  void editFunctionParameters() {
    AlertDialog.Builder alert = new AlertDialog.Builder(mainActivity);

    alert.setTitle("Function " + name);

    LinearLayout mainLayout = new LinearLayout(mainActivity);
    mainLayout.setOrientation(LinearLayout.VERTICAL);

    TextView paramLabel = new TextView(mainActivity);
    paramLabel.setText("Parameter List");
    mainLayout.addView(paramLabel);
    ((LinearLayout.LayoutParams) paramLabel.getLayoutParams()).leftMargin = 56;
    ((LinearLayout.LayoutParams) paramLabel.getLayoutParams()).rightMargin = 56;

    parameterListView = new LinearLayout(mainActivity);
    parameterListView.setOrientation(LinearLayout.VERTICAL);

    updateParameterList();

    mainLayout.addView(parameterListView);
    ((LinearLayout.LayoutParams) parameterListView.getLayoutParams()).leftMargin = 56;
    ((LinearLayout.LayoutParams) parameterListView.getLayoutParams()).rightMargin = 56;


    TypeSpinner typeInput = new TypeSpinner(mainActivity, "(none)");
    TextView typeLabel = new TextView(mainActivity);
    typeLabel.setText("Return Type");
    typeInput.selectType(returnType);
    mainLayout.addView(typeLabel);
    ((LinearLayout.LayoutParams) typeLabel.getLayoutParams()).leftMargin = 56;
    ((LinearLayout.LayoutParams) typeLabel.getLayoutParams()).rightMargin = 56;
    mainLayout.addView(typeInput);
    ((LinearLayout.LayoutParams) typeInput.getLayoutParams()).leftMargin = 56;
    ((LinearLayout.LayoutParams) typeInput.getLayoutParams()).rightMargin = 56;

    ScrollView parameterScrollView = new ScrollView(mainActivity);
    parameterScrollView.addView(mainLayout);
    alert.setView(parameterScrollView);

    alert.setNegativeButton("Cancel", null);
    alert.setPositiveButton("Ok", (a, b) -> {
      returnType = typeInput.getSelectedType();
      if (mode == Mode.CHANGE_SIGNATURE) {
        commitRefactor();
      } else {
        commitNewFunction();
      }
    });

    alert.show();
  }


  void editParameter(int index, boolean add) {
    final ParameterWithOriginalIndex parameter = add ? new ParameterWithOriginalIndex("", Types.FLOAT, -1) : parameterList.get(index);

    LinearLayout nameAndType = new LinearLayout(mainActivity);
    nameAndType.setOrientation(LinearLayout.VERTICAL);
    TextView nameLabel = new TextView(mainActivity);
    nameLabel.setText("Parameter name");
    nameAndType.addView(nameLabel);

    TextInputLayout nameInputLayout = new TextInputLayout(mainActivity);
    nameInputLayout.addView(new EditText(mainActivity));
    nameInputLayout.getEditText().setText(parameter.name);
    nameAndType.addView(nameInputLayout);

    TextView typeLabel = new TextView(mainActivity);
    typeLabel.setText("Parameter Type");
    nameAndType.addView(typeLabel);
    TypeSpinner typeInput = new TypeSpinner(mainActivity);
    typeInput.selectType(parameter.type);
    nameAndType.addView(typeInput);

    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mainActivity);

    alertBuilder.setTitle((add ? "Add Parameter " : "Edit Parameter ") + index);
    alertBuilder.setView(nameAndType);

    alertBuilder.setNegativeButton("Cancel", null);
    alertBuilder.setPositiveButton(add? "Add" : "Ok", (a, b) -> {
      parameter.name = nameInputLayout.getEditText().getText().toString();
      parameter.type = typeInput.getSelectedType();
      if (add) {
        parameterList.add(parameter);
      }
      updateParameterList();
    });

    AlertDialog alert = alertBuilder.show();


    new TextValidator() {
      @Override
      public String validate(String text) {
        String result = validateImpl(text);
        alert.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(result == null);
        return result;
      }

      private String validateImpl(String text) {
        if (text.isEmpty()) {
          return "Name must not be empty....";
        }
        if (!Character.isJavaIdentifierStart(text.charAt(0))) {
          return "'" + text.charAt(0) + "' is not a valid name start character. Parameter names should start with a lowercase letter.";
        }
        for (int i = 1; i < text.length(); i++) {
          char c = text.charAt(i);
          if (!Character.isJavaIdentifierPart(c)) {
            return "'" + c + "' is not a valid function name character. Use letters, digits and underscores.";
          }
        }
        for (ParameterWithOriginalIndex other : parameterList) {
          if (other != parameter && other.name.equals(text)) {
            return "There is already a parameter with this name.";
          }
        }
        typeInput.setEnabled(true);
        if ("self".equals(text)) {
          if (index != 0) {
            return "Parameter name 'self' only permitted for the first parameter.";
          }
          typeInput.selectType(classifier);
          typeInput.setEnabled(false);
        } else if (classifier instanceof Trait && index == 0) {
          return "First parameter must be 'self' for Trait methods.";
        }
        return null;
      }
    }.attach(nameInputLayout);

  }



  void commitRefactor() {
    // Figure out the parameter movements

    int count = parameterList.size();
    int[] oldIndices = new int[count];
    Parameter[] parameters = new Parameter[count];
    FunctionType oldType = (FunctionType) property.getType();
    boolean moved = count != oldType.getParameterCount();
    HashMap<String, String> renameMap = new HashMap<>();
    for (int i = 0; i < count; i++) {
      ParameterWithOriginalIndex parameter = parameterList.get(i);
      if (parameter.originalIndex != -1) {
        moved = true;
        if (!parameter.name.equals(oldType.getParameter(parameter.originalIndex).getName())) {
          renameMap.put(oldType.getParameter(parameter.originalIndex).getName(), parameter.name);
        }
      }
      oldIndices[i] = parameter.originalIndex;
      parameters[i] = Parameter.create(parameter.name, parameter.type);
    }

    FunctionType newType = new FunctionType(returnType, parameters);
    property.changeFunctionType(newType);
    if (moved) {
      mainActivity.program.processNodes(node -> node.reorderParameters(property, oldIndices));
    }
    if (!renameMap.isEmpty() && property.getStaticValue() instanceof UserFunction) {
      ((UserFunction) property.getStaticValue()).processNodes(node -> node.renameParameters(renameMap));
    }
    mainActivity.program.notifyProgramChanged();
  }

  void commitNewFunction() {
    FunctionType functionType = new FunctionType(returnType, parameterList.toArray(Parameter.EMPTY_ARRAY));

    Property property;
    if (classifier instanceof Trait) {
      property = TraitProperty.create((Trait) classifier, name, functionType);
    } else {
      // Module or classifier
      UserFunction userFunction = new UserFunction(mainActivity.program, functionType);
      RemStatement remStatement = new RemStatement("This comment should document this function.");
      userFunction.appendStatement(remStatement);
      property = StaticProperty.createMethod(classifier, name, userFunction);
    }
    classifier.putProperty(property);
    mainActivity.program.notifyProgramChanged();
  }

  static class ParameterWithOriginalIndex {
    final int originalIndex;
    String name;
    Type type;

    ParameterWithOriginalIndex(String name, Type type, int originalIndex) {
      this.name = name;
      this.type = type;
      this.originalIndex = originalIndex;
    }

    ParameterWithOriginalIndex(Parameter parameter, int originalIndex) {
      this(parameter.getName(), parameter.getType(), originalIndex);
    }


  }

}
