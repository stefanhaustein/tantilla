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
import org.kobjects.asde.android.ide.symbol.SymbolNameValidator;
import org.kobjects.asde.android.ide.widget.TypeSpinner;
import org.kobjects.asde.android.ide.widget.IconButton;
import org.kobjects.asde.android.ide.text.TextValidator;
import org.kobjects.asde.lang.classifier.UserClass;
import org.kobjects.asde.lang.function.UserFunction;
import org.kobjects.asde.lang.symbol.StaticSymbol;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.statement.RemStatement;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.type.Type;

import java.util.ArrayList;

public class FunctionSignatureFlow {

  public enum Mode {
    CREATE_GLOBAL, CHANGE_SIGNATURE, CREATE_MEMBER
  }

  final MainActivity mainActivity;
  private final Mode mode;
  StaticSymbol symbol;
  String name;
  Type returnType;
  ArrayList<Parameter> originalParameterList;
  ArrayList<Parameter> parameterList = new ArrayList<>();
  LinearLayout parameterListView;
  UserFunction userFunction;
  UserClass classImplementation;

  public static void changeSignature(MainActivity mainActivity, StaticSymbol symbol, UserFunction userFunction) {
    FunctionSignatureFlow flow = new FunctionSignatureFlow(mainActivity, Mode.CHANGE_SIGNATURE, userFunction.getType().getReturnType());
    flow.symbol = symbol;
    flow.name = symbol.getName();
    flow.userFunction = userFunction;
    flow.originalParameterList = new ArrayList<>();
    for (int i = 0; i < userFunction.getType().getParameterCount(); i++) {
      Parameter parameter = new Parameter();
      parameter.name = userFunction.parameterNames[i];
      parameter.type = userFunction.getType().getParameterType(i);
      flow.originalParameterList.add(parameter);
      flow.parameterList.add(parameter);
    }
    flow.editFunctionParameters();
  }

  public static void createMethod(MainActivity mainActivity, UserClass classImplementation) {
    FunctionSignatureFlow flow = new FunctionSignatureFlow(mainActivity, Mode.CREATE_MEMBER, Types.VOID);
    flow.classImplementation = classImplementation;
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



    new SymbolNameValidator(
            mode == Mode.CREATE_MEMBER ? classImplementation : mainActivity.program) {
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
    for (Parameter parameter : parameterList) {
      final int finalIndex = index;
      LinearLayout parameterView = new LinearLayout(mainActivity);

      IconButton deleteButton = new IconButton(mainActivity, R.drawable.baseline_clear_24);
      parameterView.addView(deleteButton);
      deleteButton.setOnClickListener(event -> {
        parameterList.remove(parameter);
        updateParameterList();
      });

            /*
            IconButton addButton = new IconButton(mainActivity, R.drawable.baseline_add_24);
            addButton.setOnClickListener(event -> {
                editParameter(finalIndex, true);});
            parameterView.addView(addButton);
*/
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
          parameterList.remove(parameter);
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
    Parameter parameter = add ? new Parameter() : parameterList.get(index);

    LinearLayout nameAndType = new LinearLayout(mainActivity);
    nameAndType.setOrientation(LinearLayout.VERTICAL);
    TextView nameLabel = new TextView(mainActivity);
    nameLabel.setText("Parameter name");
    nameAndType.addView(nameLabel);

    TextInputLayout nameInput = new TextInputLayout(mainActivity);
    nameInput.addView(new EditText(mainActivity));
    nameInput.getEditText().setText(parameter.name);
    nameAndType.addView(nameInput);

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
      parameter.name = nameInput.getEditText().getText().toString();
      parameter.type = typeInput.getSelectedType();
      if (add) {
        parameterList.add(index, parameter);
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

      public String validateImpl(String text) {
        System.err.println("Validation Text: '" + text + "' len: " + text.length());
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
        for (Parameter other : parameterList) {
          if (other != parameter && other.name.equals(text)) {
            return "There is already a parameter with this name.";
          }
        }

        return null;
      }
    }.attach(nameInput);

  }

  Type[] getParameterTypeArray() {
    Type[] result = new Type[parameterList.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = parameterList.get(i).type;
    }
    return result;
  }

  String[] getParameterNameArray() {
    String[] result = new String[parameterList.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = parameterList.get(i).name;
    }
    return result;
  }


  void commitRefactor() {
    // Figure out the parameter movements

    int count = parameterList.size();
    int[] oldIndices = new int[count];
    boolean moved = count != originalParameterList.size();

    for (int i = 0; i < count; i++) {
      int oldIndex = originalParameterList.indexOf(parameterList.get(i));
      oldIndices[i] = oldIndex;
      if (oldIndex != i) {
        moved = true;
      }
    }

    Type[] types = new Type[count];
    userFunction.parameterNames = new String[parameterList.size()];
    for (int i = 0; i < parameterList.size(); i++) {
      Parameter parameter = parameterList.get(i);
      userFunction.parameterNames[i] = parameter.name;
      types[i] = parameter.type;
    }

    userFunction.setType(new FunctionType(returnType, types));

    if (moved) {
      mainActivity.program.processNodes(node -> node.changeSignature(symbol, oldIndices));
    }

    mainActivity.program.notifyProgramChanged();
  }

  void commitNewFunction() {
    FunctionType functionType = new FunctionType(returnType, getParameterTypeArray());
    UserFunction userFunction = new UserFunction(mainActivity.program, functionType, getParameterNameArray());

    RemStatement remStatement = new RemStatement("This comment should document this function.");

    userFunction.appendStatement(remStatement);

    if (mode == Mode.CREATE_MEMBER) {
      classImplementation.setMethod(name, userFunction);
    } else {
      mainActivity.program.setDeclaration(name, userFunction);
    }
    mainActivity.program.notifyProgramChanged();
  }

}
