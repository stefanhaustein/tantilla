package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.EvaluationContext;

public abstract class AssignableNode extends Node {

    AssignableNode(Node... children) {
        super(children);
    }


    public abstract void set(EvaluationContext evaluationContext, Object value);

    public abstract boolean isConstant();
}
