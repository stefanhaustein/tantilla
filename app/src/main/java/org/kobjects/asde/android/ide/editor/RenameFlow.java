package org.kobjects.asde.android.ide.editor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.design.widget.TextInputLayout;
import android.widget.EditText;

import org.kobjects.asde.android.ide.MainActivity;

public class RenameFlow {

    private final MainActivity mainActivity;
    private final String oldName;

    public RenameFlow(MainActivity mainActivity, String oldName) {
        this.mainActivity = mainActivity;
        this.oldName = oldName;
    }

    public void start() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mainActivity);
        alertBuilder.setTitle("Rename Symbol");
        alertBuilder.setMessage("Name");
        TextInputLayout nameInput = new TextInputLayout(mainActivity);
        nameInput.addView(new EditText(mainActivity));
        nameInput.getEditText().setText(oldName);
        nameInput.setErrorEnabled(true);
        alertBuilder.setView(nameInput);

        alertBuilder.setNegativeButton("Cancel", null);
        alertBuilder.setPositiveButton("Rename", (a,b) -> {
            mainActivity.program.renameGlobalSymbol(oldName, nameInput.getEditText().getText().toString());
            mainActivity.sync(true);
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
}
