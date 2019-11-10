package org.kobjects.annotatedtext;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class AnnotatedString implements CharSequence {
    private final String text;
    private final Set<Span> spans;

    private AnnotatedString(String text) {
        this.text = text;
        this.spans = Collections.emptySet();
    }

    public AnnotatedString(String text, Collection<Span> spans) {
        this.text = text;
        this.spans = new LinkedHashSet<>(spans);
    }

    public static AnnotatedString of(CharSequence chars) {
        return chars == null ? null : chars instanceof AnnotatedString ? (AnnotatedString) chars : new AnnotatedString(chars.toString());
    }

    @Override
    public int length() {
        return text.length();
    }

    @Override
    public char charAt(int i) {
        return text.charAt(i);
    }

    @Override
    public AnnotatedString subSequence(int start, int end) {
        LinkedHashSet<Span> subSpans = new LinkedHashSet<>();
        for (Span span : spans) {
            if (span.start < end && span.end >= start) {
                subSpans.add(new Span(Math.max(span.start - start, 0),
                        Math.min(span.end -start, end), span.annotation));
            }
        }
        return new AnnotatedString(text.substring(start, end), subSpans);
    }


    public Iterable<Span> spans() {
        return spans;
    }

    @Override
    public String toString() {
        return text;
    }

    public int indexOf(char c) {
        return text.indexOf(c);
    }
}
