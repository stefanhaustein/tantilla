package org.kobjects.asde.android.ide.editor;

import android.app.AlertDialog;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.kobjects.asde.R;
import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.widget.IconButton;
import org.kobjects.asde.android.ide.widget.TextValidator;
import org.kobjects.asde.lang.CallableUnit;
import org.kobjects.asde.lang.CodeLine;
import org.kobjects.asde.lang.Types;
import org.kobjects.asde.lang.statement.RemStatement;
import org.kobjects.asde.lang.symbol.GlobalSymbol;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Type;

import java.util.ArrayList;
import java.util.Collections;

public class FunctionSignatureBuilder {

    final MainActivity mainActivity;
    String name;
    Type returnType;
    ArrayList<Parameter> parameterList = new ArrayList<>();
    LinearLayout parameterListView;


    public FunctionSignatureBuilder(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void createFunction() {
        createCallableUnit(false);
    }

    public void createSubroutine() {
        createCallableUnit(true);
    }


    void createCallableUnit(boolean isVoid) {
        LinearLayout nameAndType = new LinearLayout(mainActivity);
        nameAndType.setOrientation(LinearLayout.VERTICAL);
        TextView nameLabel = new TextView(mainActivity);
        nameLabel.setText("Name");
        nameAndType.addView(nameLabel);

        EditText nameInput = new EditText(mainActivity);
        nameInput.setText(name);
        nameAndType.addView(nameInput);

        TextView errorTextView = new TextView(mainActivity);
        errorTextView.setText("Name must not be empty.");
        errorTextView.setTextColor(mainActivity.colors.accent);
        nameAndType.addView(errorTextView);

        boolean[] inputValid = new boolean[1];

        nameInput.addTextChangedListener(new TextValidator(nameInput) {
            @Override
            public void validate(TextView textView, String text) {
                if (text.isEmpty()) {
                    errorTextView.setText("Name must not be empty.");
                    inputValid[0] = false;
                } else if (!Character.isJavaIdentifierStart(text.charAt(0))) {
                    errorTextView.setText("'" + text.charAt(0) + "' is not a valid name start character. Function names should start with a lowercase letter.");
                    inputValid[0] = false;
                } else if (mainActivity.program.getSymbol(text) != null) {
                    errorTextView.setText("Name exists already.");
                    inputValid[0] = false;
                } else {
                    for (int i = 1; i < text.length(); i++) {
                        char c = text.charAt(i);
                        if (!Character.isJavaIdentifierPart(c)) {
                            errorTextView.setText("'" + c + "' is not a valid function name character. Use letters, digits and underscores.");
                            inputValid[0] = false;
                            return;
                        }
                    }
                    errorTextView.setText("");
                    inputValid[0] = true;
                }
            }
        });


        TypeSpinner typeInput = isVoid ? null : new TypeSpinner(mainActivity);
        if (!isVoid) {
            TextView typeLabel = new TextView(mainActivity);
            typeLabel.setText("Return Type");
            nameAndType.addView(typeLabel);
            nameAndType.addView(typeInput);
        }
        AlertDialog.Builder alert = new AlertDialog.Builder(mainActivity);

        alert.setTitle(isVoid ? "New Subroutine" : "New Function");
        alert.setView(nameAndType);

        alert.setNegativeButton("Cancel", null);

        alert.setPositiveButton("Next", (a, b) -> {
            name = nameInput.getText().toString();
            returnType = isVoid ? Types.VOID : typeInput.getSelectedType();

            if (inputValid[0]) {
                editFunctionParameters();
            } else {
                createCallableUnit(isVoid);
            }
        });
        alert.show();
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

            parameterListView.addView(parameterView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
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
        alert.setMessage("Parameters");

        parameterListView = new LinearLayout(mainActivity);
        parameterListView.setOrientation(LinearLayout.VERTICAL);

       updateParameterList();

        ScrollView parameterScrollView = new ScrollView(mainActivity);
        parameterScrollView.addView(parameterListView);

        alert.setView(parameterScrollView);

        alert.setNegativeButton("Cancel", (a,b) -> {
        });


        alert.setPositiveButton("Ok", (a, b) -> {
            commitNewFunction();
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

        EditText nameInput = new EditText(mainActivity);
        nameInput.setText(parameter.name);
        nameAndType.addView(nameInput);

        TextView errorTextView = new TextView(mainActivity);
        errorTextView.setText("Name must not be empty.");
        nameAndType.addView(errorTextView);

        boolean[] inputValid = new boolean[1];

        nameInput.addTextChangedListener(new TextValidator(nameInput) {
            @Override
            public void validate(TextView textView, String text) {
                System.err.println("Validation Text: '" + text + "' len: " + text.length());
                if (text.isEmpty()) {
                    errorTextView.setText("Name must not be empty....");
                    inputValid[0] = false;
                } else if (!Character.isJavaIdentifierStart(text.charAt(0))) {
                    errorTextView.setText("'" + text.charAt(0) + "' is not a valid name start character. Parameter names should start with a lowercase letter.");
                    inputValid[0] = false;
                } else {
                    for (int i = 1; i < text.length(); i++) {
                        char c = text.charAt(i);
                        if (!Character.isJavaIdentifierPart(c)) {
                            errorTextView.setText("'" + c + "' is not a valid function name character. Use letters, digits and underscores.");
                            inputValid[0] = false;
                            return;
                        }
                    }
                    errorTextView.setText("");
                    inputValid[0] = true;
                }
            }
        });

        TextView typeLabel = new TextView(mainActivity);
        typeLabel.setText("Parameter Type");
        nameAndType.addView(typeLabel);
        TypeSpinner typeInput = new TypeSpinner(mainActivity);
        typeInput.selectType(parameter.type);
        nameAndType.addView(typeInput);

        AlertDialog.Builder alert = new AlertDialog.Builder(mainActivity);

        alert.setTitle("Add Parameter");
        alert.setView(nameAndType);

        alert.setNegativeButton("Cancel", (a, b) -> {

        });

        alert.setPositiveButton(add? "Add" : "Ok", (a, b) -> {
            parameter.name = nameInput.getText().toString();
            parameter.type = typeInput.getSelectedType();
            if (add) {
                parameterList.add(index, parameter);
            }
            updateParameterList();
        });

        alert.show();
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


    void commitNewFunction() {
        FunctionType functionType = new FunctionType(returnType, getParameterTypeArray());
        CallableUnit callableUnit = new CallableUnit(mainActivity.program, functionType, getParameterNameArray());

        RemStatement remStatement = new RemStatement("TBD");

        callableUnit.setLine(10, new CodeLine(Collections.singletonList(remStatement)));

        mainActivity.program.setValue(GlobalSymbol.Scope.PERSISTENT, name, callableUnit);

        mainActivity.sync(true);
    }

}
