package org.kobjects.asde.android.ide.property;

import org.kobjects.asde.android.ide.text.TextValidator;
import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.classifier.Property;

public class PropertyNameValidator extends TextValidator {
    private Classifier classifier;

    public PropertyNameValidator(Classifier symbolOwner) {
        this.classifier = symbolOwner;

    }

    @Override
    public String validate(String text) {
        if (text.isEmpty()) {
            return "Name must not be empty.";
        }
        if (!Character.isJavaIdentifierStart(text.charAt(0))) {
            return "'" + text.charAt(0) + "' is not a valid name start character. Function names should start with a lowercase letter.";
        }
        Property existing = classifier.getProperty(text);
        if (existing != null) {
            System.out.println("Existing: " + existing);
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
