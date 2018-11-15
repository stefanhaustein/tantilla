package org.kobjects.annotatedtext;

import java.util.HashSet;
import java.util.LinkedHashSet;

public class AnnotatedStringBuilder implements Appendable {
    private final StringBuilder sb;
    private final LinkedHashSet<Span> spans = new LinkedHashSet<>();

    public AnnotatedStringBuilder() {
        sb = new StringBuilder();
    }

    @Override
    public AnnotatedStringBuilder append(CharSequence charSequence) {
        int offset = sb.length();
        sb.append(charSequence);
        if (charSequence instanceof AnnotatedString) {
            for (Span span : ((AnnotatedString) charSequence).spans()) {
                spans.add(new Span(span.start + offset, span.end + offset, span.annotation));
            }
        }
        return this;
    }

    // This drops annotations "below". Should they be merged instead?
    public AnnotatedStringBuilder append(CharSequence charSequence, Object annotation) {
        if (annotation != null) {
            spans.add(new Span(sb.length(), sb.length() + charSequence.length(), annotation));
        }
        sb.append(charSequence);
        return this;
    }


    // TODO: fix annotated string case
    @Override
    public AnnotatedStringBuilder append(CharSequence charSequence, int start, int end) {
        if (charSequence instanceof AnnotatedString) {
            append(charSequence.subSequence(start, end));
        } else {
            sb.append(charSequence, start, end);
        }
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

    public AnnotatedString build() {
        return new AnnotatedString(sb.toString(), spans);
    }
}
