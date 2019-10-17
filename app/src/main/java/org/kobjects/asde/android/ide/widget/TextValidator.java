package org.kobjects.asde.android.ide.widget;

import com.google.android.material.textfield.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;

public abstract class TextValidator {

  public abstract String validate(String text);


  public void attach(TextInputLayout textInputLayout) {
    new TextInputLayoutValidator(textInputLayout);
  }

  class TextInputLayoutValidator implements TextWatcher {
    private final TextInputLayout textInputLayout;

    public TextInputLayoutValidator(TextInputLayout textInputLayout) {
      this.textInputLayout = textInputLayout;
      textInputLayout.getEditText().addTextChangedListener(this);
      textInputLayout.post(() -> afterTextChanged(textInputLayout.getEditText().getText()));
    }


    @Override
    final public void afterTextChanged(Editable s) {
      String text = textInputLayout.getEditText().getText().toString();
      String error = validate(text);

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