package org.kobjects.annotatedtext;

public class Span {
    public final int start;
    public final int end;
    public final Object annotation;

    Span(int start, int end, Object annotation) {
        this.start = start;
        this.end = end;
        this.annotation = annotation;
    }
}
