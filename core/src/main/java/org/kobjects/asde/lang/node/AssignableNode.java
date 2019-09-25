package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.typesystem.Type;

public abstract class AssignableNode extends Node {

    AssignableNode(Node... children) {
        super(children);
    }

    public abstract void resolveForAssignment(FunctionValidationContext resolutionContext, Type type, int line, int index);

    public abstract void set(EvaluationContext evaluationContext, Object value);

    public abstract boolean isConstant();
}
