package org.kobjects.asde.android.ide.editor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.design.widget.TextInputLayout;
import android.widget.EditText;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.lang.StaticSymbol;
import org.kobjects.asde.lang.SymbolOwner;
import org.kobjects.asde.lang.refactor.Rename;

public class RenameFlow {

  private final MainActivity mainActivity;
  private final StaticSymbol symbol;

  public RenameFlow(MainActivity mainActivity, StaticSymbol symbol) {
    this.mainActivity = mainActivity;
    this.symbol = symbol;
  }

  public void start() {
    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mainActivity);
    alertBuilder.setTitle("Rename Symbol");
    alertBuilder.setMessage("Name");
    TextInputLayout nameInput = new TextInputLayout(mainActivity);
    nameInput.addView(new EditText(mainActivity));
    nameInput.getEditText().setText(symbol.getName());
    nameInput.setErrorEnabled(true);
    alertBuilder.setView(nameInput);

    alertBuilder.setNegativeButton("Cancel", null);
    alertBuilder.setPositiveButton("Rename", (a,b) -> {
      String newName = nameInput.getEditText().getText().toString().trim();
      if (newName != null && !newName.isEmpty() && !newName.equals(symbol.getName())) {
        SymbolOwner owner = symbol.getOwner();
        owner.removeSymbol(symbol);
        String oldName = symbol.getName();
        symbol.setName(newName);
        owner.addSymbol(symbol);
        mainActivity.program.accept(new Rename(symbol, oldName, newName));
      }
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
