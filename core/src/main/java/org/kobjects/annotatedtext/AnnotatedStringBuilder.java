package org.kobjects.annotatedtext;

import java.io.IOException;

public class AnnotatedStringBuilder implements Appendable {
    private final StringBuilder sb;

    public AnnotatedStringBuilder() {
        sb = new StringBuilder();
    }

    @Override
    public AnnotatedStringBuilder append(CharSequence charSequence) {
        sb.append(charSequence);
        return this;
    }

    @Override
    public AnnotatedStringBuilder append(CharSequence charSequence, int i, int i1) {
        sb.append(charSequence, i, i1);
        return this;
    }

    @Override
    public AnnotatedStringBuilder append(char c) {
        sb.append(c);
        return this;
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
