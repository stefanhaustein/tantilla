package org.kobjects.asde.android.ide.editor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.design.widget.TextInputLayout;
import android.widget.EditText;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.widget.TextValidator;

public class InputFlowBuilder {
  private final MainActivity mainActivity;
  private final String title;
  private final ResultHandler resultHandler;

  private String value;
  private ValidatorFactory validatorFactory;
  private String label;
  private String positiveLabel = "Ok";
  private String negativeLabel = "Cancel";


  public InputFlowBuilder(MainActivity mainActivity, String title, ResultHandler resultHandler) {
    this.mainActivity = mainActivity;
    this.title = title;
    this.resultHandler = resultHandler;
  }

  public InputFlowBuilder setValidatorFactory(ValidatorFactory validatorFactory) {
    this.validatorFactory = validatorFactory;
    return this;
  }

  public InputFlowBuilder setLabel(String label) {
    this.label = label;
    return this;
  }

  public InputFlowBuilder setValue(String value) {
    this.value = value;
    return this;
  }

  public InputFlowBuilder setPositiveLabel(String label) {
    this.positiveLabel = label;
    return this;
  }


  public void start() {
    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mainActivity);
    alertBuilder.setTitle(title);
    if (label != null) {
      alertBuilder.setMessage(label);
    }
    TextInputLayout inputLayout = new TextInputLayout(mainActivity);
    inputLayout.addView(new EditText(mainActivity));
    if (value != null) {
      inputLayout.getEditText().setText(value);
    }
    inputLayout.setErrorEnabled(true);
    alertBuilder.setView(inputLayout);

    alertBuilder.setNegativeButton(negativeLabel, null);
    alertBuilder.setPositiveButton(positiveLabel, (a, b) -> {
      resultHandler.handleResult(inputLayout.getEditText().getText().toString());
    });

    AlertDialog alert = alertBuilder.show();

    if (validatorFactory != null) {
      TextValidator validator = validatorFactory.createValidator(inputLayout);
      inputLayout.getEditText().addTextChangedListener(new TextValidator(inputLayout) {
        @Override
        public String validate(String text) {
          String error = validator.validate(text);
          alert.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(error == null);
          return error;
        }
      });
    }

  }
  public interface ResultHandler {
    void handleResult(String result);
  }

  public interface ValidatorFactory {
    TextValidator createValidator(TextInputLayout inputLayout);
  }
}
