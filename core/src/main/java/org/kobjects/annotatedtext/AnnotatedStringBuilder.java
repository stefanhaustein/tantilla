package org.kobjects.annotatedtext;

import java.util.HashSet;

public class AnnotatedStringBuilder implements Appendable {
    private final StringBuilder sb;
    private final HashSet<Span> spans = new HashSet<>();

    public AnnotatedStringBuilder() {
        sb = new StringBuilder();
    }

    @Override
    public AnnotatedStringBuilder append(CharSequence charSequence) {
        sb.append(charSequence);
        return this;
    }

    public AnnotatedStringBuilder append(CharSequence charSequence, Object annotation) {
        if (annotation != null) {
            spans.add(new Span(sb.length(), sb.length() + charSequence.length(), annotation));
        }
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

    public Iterable<Span> spans() {
        return spans;
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
