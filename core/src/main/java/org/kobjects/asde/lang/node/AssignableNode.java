package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Type;

public abstract class AssignableNode extends Node implements Assignable {

    AssignableNode(Node... children) {
        super(children);
    }

    public abstract Type resolveForAssignment(ValidationContext resolutionContext, int line);

}
