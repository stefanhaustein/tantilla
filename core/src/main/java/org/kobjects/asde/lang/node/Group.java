package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.type.Type;

public class Group extends Node {
    public Group(Node child) {
        super(child);
    }

    @Override
    public Object eval(Interpreter interpreter) {
        return children[0].eval(interpreter);
    }

    @Override
    public Type returnType() {
        return children[0].returnType();
    }

    @Override
    public String toString() {
        return "(" + children[0] + ")";
    }
}
