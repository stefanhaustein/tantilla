package org.kobjects.asde.android.ide.editor;

import android.support.design.widget.TextInputLayout;
import android.widget.EditText;
import android.widget.TextView;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.widget.TextValidator;
import org.kobjects.asde.lang.SymbolOwner;

class SymbolNameValidator extends TextValidator {
    private SymbolOwner symbolOwner;

    public SymbolNameValidator(SymbolOwner symbolOwner, TextInputLayout textInputLayout) {
        super(textInputLayout);
        this.symbolOwner = symbolOwner;

    }

    @Override
    public String validate(String text) {
        if (text.isEmpty()) {
            return "Name must not be empty.";
        }
        if (!Character.isJavaIdentifierStart(text.charAt(0))) {
            return "'" + text.charAt(0) + "' is not a valid name start character. Function names should start with a lowercase letter.";
        }
        if (symbolOwner.getSymbol(text) != null) {
            return "Name exists already.";
        }

        for (int i = 1; i < text.length(); i++) {
                char c = text.charAt(i);
                if (!Character.isJavaIdentifierPart(c)) {
                    return "'" + c + "' is not a valid function name character. Use letters, digits and underscores.";
                }
            }
        return null;
    }
}
