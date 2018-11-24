package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.node.Node;

public class EvaluationException extends RuntimeException {

    public final Node node;

    public EvaluationException(Node node, String msg) {
        super(msg);
        this.node = node;
    }
}
