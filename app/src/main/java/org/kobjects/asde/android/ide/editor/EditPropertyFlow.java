package org.kobjects.asde.android.ide.editor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.design.widget.TextInputLayout;
import android.widget.EditText;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.lang.ClassImplementation;

public class EditPropertyFlow {

  private final MainActivity mainActivity;
  private final ClassImplementation.ClassPropertyDescriptor symbol;

  public EditPropertyFlow(MainActivity mainActivity, ClassImplementation.ClassPropertyDescriptor symbol) {
    this.mainActivity = mainActivity;
    this.symbol = symbol;
  }

  public void start() {
    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mainActivity);
    alertBuilder.setTitle("Edit Property " + symbol.getName());
    alertBuilder.setMessage("Initial value");
    TextInputLayout input = new TextInputLayout(mainActivity);
    input.addView(new EditText(mainActivity));
    input.getEditText().setText(symbol.getInitializer().toString());
    input.setErrorEnabled(true);
    alertBuilder.setView(input);

    alertBuilder.setNegativeButton("Cancel", null);
    alertBuilder.setPositiveButton("Ok", (a,b) -> {
      String unparsed = input.getEditText().getText().toString().trim();
      if (unparsed != null && !unparsed.isEmpty() && !unparsed.equals(symbol.getName())) {
        symbol.setInitializer(mainActivity.program.parser.parseExpression(unparsed));
        mainActivity.program.notifySymbolChanged(symbol);
      }
    });

    AlertDialog alert = alertBuilder.show();

    input.getEditText().addTextChangedListener(new ExpressionValidator(mainActivity, input) {
      @Override
      public String validate(String text) {
        String result = super.validate(text);
        alert.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(result == null);
        return result;
      }
    });
  }
}
