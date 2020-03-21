package org.kobjects.asde.android.ide.text;

import com.google.android.material.textfield.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;

public abstract class TextValidator {

  public abstract String validate(String text);


  public TextInputLayoutValidator attach(TextInputLayout textInputLayout) {
    return new TextInputLayoutValidator(textInputLayout);
  }

  public class TextInputLayoutValidator implements TextWatcher {
    private final TextInputLayout textInputLayout;
    public boolean enabled = true;
    String error;

    public boolean update() {
      afterTextChanged(textInputLayout.getEditText().getText());
      return error == null;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
      if (!enabled) {
        textInputLayout.setError(null);
      } else {
        update();
      }
    }


    public TextInputLayoutValidator(TextInputLayout textInputLayout) {
      this.textInputLayout = textInputLayout;
      textInputLayout.getEditText().addTextChangedListener(this);
      textInputLayout.post(() -> afterTextChanged(textInputLayout.getEditText().getText()));
    }


    @Override
    final public void afterTextChanged(Editable s) {
      if (!enabled) {
        return;
      }
      String text = textInputLayout.getEditText().getText().toString();
      error = validate(text);
      textInputLayout.setError(error);
    }

    @Override
    final public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      // required but unused
    }

    @Override
    final public void onTextChanged(CharSequence s, int start, int before, int count) {
      // required but unused
    }
  }
}