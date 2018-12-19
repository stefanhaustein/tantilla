package org.kobjects.asde.android.ide.editor;

import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.widget.TextValidator;
import org.kobjects.asde.lang.CallableUnit;
import org.kobjects.asde.lang.CodeLine;
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


    public FunctionSignatureBuilder(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }


    public void createFunction() {

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
        typeLabel.setText("Return Type");
        nameAndType.addView(typeLabel);
        TypeSpinner typeInput = new TypeSpinner(mainActivity);
        nameAndType.addView(typeInput);

        AlertDialog.Builder alert = new AlertDialog.Builder(mainActivity);

        alert.setTitle("New Function");
        alert.setView(nameAndType);

        alert.setNegativeButton("Cancel", null);

        alert.setPositiveButton("Next", (a, b) -> {
            name = nameInput.getText().toString();
            returnType = typeInput.getSelectedType();

            if (inputValid[0]) {
                editFunctionParameters();
            } else {
                createFunction();
            }
        });

        alert.show();

    }

    void editFunctionParameters() {

        AlertDialog.Builder alert = new AlertDialog.Builder(mainActivity);

        alert.setTitle("Function " + name);

        LinearLayout parameterListView = new LinearLayout(mainActivity);

        for (Parameter parameter : parameterList) {
            LinearLayout paremeterView = new LinearLayout(mainActivity);
            TextView textView = new TextView(mainActivity);
            textView.setText(parameter.name + ": " + parameter.type);
            paremeterView.addView(textView);
            parameterListView.addView(paremeterView);
        }


        ScrollView parameterScrollView = new ScrollView(mainActivity);
        parameterScrollView.addView(parameterListView);

        alert.setView(parameterScrollView);

        alert.setNegativeButton("Back", (a,b) -> {
            createFunction();
        });

        alert.setNeutralButton("Add Parameter", (a, b) -> {
            addParameter();
        });

        alert.setPositiveButton("Ok", (a, b) -> {

            commitNewFunction();

        });


        alert.show();

    }


    void addParameter() {
        LinearLayout nameAndType = new LinearLayout(mainActivity);
        nameAndType.setOrientation(LinearLayout.VERTICAL);
        TextView nameLabel = new TextView(mainActivity);
        nameLabel.setText("Parameter name");
        nameAndType.addView(nameLabel);

        EditText nameInput = new EditText(mainActivity);
        nameInput.setText(name);
        nameAndType.addView(nameInput);

        TextView errorTextView = new TextView(mainActivity);
        errorTextView.setText("Name must not be empty.");
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
        nameAndType.addView(typeInput);

        AlertDialog.Builder alert = new AlertDialog.Builder(mainActivity);

        alert.setTitle("Add Parameter");
        alert.setView(nameAndType);

        alert.setNegativeButton("Cancel", (a, b) -> {
            editFunctionParameters();
        });

        alert.setPositiveButton("Add", (a, b) -> {
            Parameter parameter = new Parameter();
            parameter.name = nameInput.getText().toString();
            parameter.type = typeInput.getSelectedType();
            parameterList.add(parameter);

            editFunctionParameters();
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
