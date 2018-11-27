package org.kobjects.asde.lang.statement;

import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.parser.ResolutionContext;
import org.kobjects.typesystem.Type;

public class EndIfStatement extends Node {

    @Override
    protected void onResolve(ResolutionContext resolutionContext) {
        resolutionContext.endBlock(ResolutionContext.BlockType.IF);
    }

    @Override
    public Object eval(Interpreter interpreter) {
        return null;
    }

    @Override
    public Type returnType() {
        return null;
    }
}
