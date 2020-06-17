package org.kobjects.markdown;

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

    public AnnotatedStringBuilder annotate(int start, int end, Object annotation) {
        spans.add(new Span(start, end, annotation));
        return this;
    }

    public AnnotatedStringBuilder append(CharSequence charSequence, Object... annotations) {
        int start = sb.length();
        append(charSequence);
        for (Object annotation : annotations) {
            if (annotation != null) {
                annotate(start, sb.length(), annotation);
            }
        }
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

    public int length() {
        return sb.length();
    }

    public AnnotatedString build() {
        return new AnnotatedString(sb.toString(), spans);
    }
}
