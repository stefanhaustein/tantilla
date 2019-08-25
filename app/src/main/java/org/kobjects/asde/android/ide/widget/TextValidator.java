package org.kobjects.asde.android.ide.widget;

import com.google.android.material.textfield.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

public abstract class TextValidator implements TextWatcher {
    protected final TextInputLayout textInputLayout;

    public TextValidator(TextInputLayout textInputLayout) {
        this.textInputLayout = textInputLayout;

        textInputLayout.post(() -> afterTextChanged(textInputLayout.getEditText().getText()));
    }

    public abstract String validate(String text);

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