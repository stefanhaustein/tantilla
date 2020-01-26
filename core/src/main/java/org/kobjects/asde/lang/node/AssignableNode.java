package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.typesystem.Type;

public abstract class AssignableNode extends Node {

    AssignableNode(Node... children) {
        super(children);
    }

    public abstract void resolveForAssignment(FunctionValidationContext resolutionContext, Node parent, Type type, int line);

    public abstract void set(EvaluationContext evaluationContext, Object value);

    public abstract boolean isConstant();

    public abstract boolean isAssignable();
}
