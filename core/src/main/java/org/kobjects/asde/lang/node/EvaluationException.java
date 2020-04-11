package org.kobjects.asde.lang.node;

public class EvaluationException extends RuntimeException {

    public final Node node;

    public EvaluationException(Node node, String msg) {
        super(msg + " in " + node);
        this.node = node;
    }
}
