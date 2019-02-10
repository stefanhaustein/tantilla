package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.Interpreter;

public abstract class AssignableNode extends Node {

    AssignableNode(Node... children) {
        super(children);
    }


    public abstract void set(Interpreter interpreter, Object value);

    public abstract boolean isConstant();
}
