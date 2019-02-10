package org.kobjects.asde.android.ide.editor;

import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.support.design.widget.TextInputLayout;
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
import org.kobjects.asde.lang.type.FunctionImplementation;
import org.kobjects.asde.lang.type.CodeLine;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.refactor.ChangeSignature;
import org.kobjects.asde.lang.statement.RemStatement;
import org.kobjects.asde.lang.GlobalSymbol;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Type;

import java.util.ArrayList;
import java.util.Collections;

public class FunctionSignatureFlow {

    final MainActivity mainActivity;
    String name;
    Type returnType;
    ArrayList<Parameter> originalParameterList;
    ArrayList<Parameter> parameterList = new ArrayList<>();
    LinearLayout parameterListView;
    FunctionImplementation functionImplementation;

    public FunctionSignatureFlow(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void createFunction() {
        returnType = Types.NUMBER;
        createCallableUnit();
    }

    public void createSubroutine() {
        returnType = Types.VOID;
        createCallableUnit();
    }


    public void changeSignature(String name, FunctionImplementation functionImplementation) {
        this.name = name;
        this.functionImplementation = functionImplementation;
        this.returnType = functionImplementation.getType().getReturnType();
        originalParameterList = new ArrayList<>();
        for (int i = 0; i < functionImplementation.getType().getParameterCount(); i++) {
            Parameter parameter = new Parameter();
            parameter.name = functionImplementation.parameterNames[i];
            parameter.type = functionImplementation.getType().getParameterType(i);
            originalParameterList.add(parameter);
            parameterList.add(parameter);
        }
        editFunctionParameters();
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

        alertBuilder.setTitle(returnType == Types.VOID ? "New Subroutine" : "New Function");
        alertBuilder.setView(mainView);

        alertBuilder.setNegativeButton("Cancel", null);

        alertBuilder.setPositiveButton("Next", (a, b) -> {
            name = nameInput.getEditText().getText().toString();
            editFunctionParameters();
        });
        AlertDialog alert = alertBuilder.show();


        nameInput.getEditText().addTextChangedListener(new SymbolNameValidator(mainActivity, nameInput) {
            @Override
            public String validate(String text) {
                String result = super.validate(text);
                alert.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(result == null);
                return result;
            }
        });


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

        LinearLayout mainLayout = new LinearLayout(mainActivity);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        TextView paramLabel = new TextView(mainActivity);
        paramLabel.setText("Parameter List");
        mainLayout.addView(paramLabel);

        parameterListView = new LinearLayout(mainActivity);
        parameterListView.setOrientation(LinearLayout.VERTICAL);

        updateParameterList();

        mainLayout.addView(parameterListView);



        TypeSpinner typeInput = returnType == Types.VOID ? null : new TypeSpinner(mainActivity);
        if (returnType != Types.VOID) {
            TextView typeLabel = new TextView(mainActivity);
            typeLabel.setText("Return Type");
            typeInput.selectType(returnType);
            mainLayout.addView(typeLabel);
            mainLayout.addView(typeInput);
        }

        ScrollView parameterScrollView = new ScrollView(mainActivity);
        parameterScrollView.addView(mainLayout);
        alert.setView(parameterScrollView);

        alert.setNegativeButton("Cancel", null);
        alert.setPositiveButton("Ok", (a, b) -> {
            if (typeInput != null) {
                returnType = typeInput.getSelectedType();
            }
            if (functionImplementation == null) {
                commitNewFunction();
            } else {
                commitRefactor();
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


        nameInput.getEditText().addTextChangedListener(new TextValidator(nameInput) {
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
        });

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
        // Figure out the paramter movements

        int count = parameterList.size();
        int[] oldIndices = new int[count];
        boolean changed = count != originalParameterList.size();

        for (int i = 0; i < count; i++) {
            int oldIndex = originalParameterList.indexOf(parameterList.get(i));
            oldIndices[i] = oldIndex;
            if (oldIndex != i) {
                changed = true;
            }
        }

        if (!changed) {
            return;
        }

        // Refactor

        Type[] types = new Type[count];
        functionImplementation.parameterNames = new String[parameterList.size()];
        for (int i = 0; i < parameterList.size(); i++) {
            Parameter parameter = parameterList.get(i);
            functionImplementation.parameterNames[i] = parameter.name;
            types[i] = parameter.type;
        }

        functionImplementation.setType(new FunctionType(functionImplementation.getType().getReturnType(), types));

        mainActivity.program.accept(new ChangeSignature(name, oldIndices));
    }

    void commitNewFunction() {
        FunctionType functionType = new FunctionType(returnType, getParameterTypeArray());
        FunctionImplementation functionImplementation = new FunctionImplementation(mainActivity.program, functionType, getParameterNameArray());

        RemStatement remStatement = new RemStatement("This comment should document this function.");

        functionImplementation.setLine(new CodeLine(10, remStatement));

        mainActivity.program.setPersistentFunction(name, functionImplementation);
    }

}
