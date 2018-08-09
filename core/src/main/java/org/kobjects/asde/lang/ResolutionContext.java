package org.kobjects.asde.lang;


import org.kobjects.asde.lang.node.Node;

import java.util.HashMap;

public class ResolutionContext {
    Program program;
    HashMap<Node, Exception> errors = new HashMap<>();

    ResolutionContext(Program program) {
        this.program = program;
    }

    public Symbol getSymbol(String name) {
        return program.getSymbol(name);
    }

    public void addError(Node node, Exception e) {
        errors.put(node, e);
    }
}
