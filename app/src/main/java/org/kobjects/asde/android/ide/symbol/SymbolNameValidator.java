package org.kobjects.asde.android.ide.symbol;

import org.kobjects.asde.android.ide.text.TextValidator;
import org.kobjects.asde.lang.program.GlobalSymbol;
import org.kobjects.asde.lang.symbol.StaticSymbol;
import org.kobjects.asde.lang.symbol.SymbolOwner;

public class SymbolNameValidator extends TextValidator {
    private SymbolOwner symbolOwner;

    public SymbolNameValidator(SymbolOwner symbolOwner) {
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
        StaticSymbol existing = symbolOwner.getSymbol(text);
        if (existing != null && existing.getScope() != GlobalSymbol.Scope.TRANSIENT) {
            System.out.println("Existing: " + symbolOwner.getSymbol(text));
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
