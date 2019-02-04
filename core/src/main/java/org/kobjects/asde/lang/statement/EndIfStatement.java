package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class EndIfStatement extends Node {

    @Override
    protected void onResolve(FunctionValidationContext resolutionContext, int line, int index) {
        resolutionContext.endBlock(FunctionValidationContext.BlockType.IF);
    }

    @Override
    public Object eval(Interpreter interpreter) {
        return null;
    }

    @Override
    public Type returnType() {
        return null;
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
        appendLinked(asb, "ENDIF", errors);
    }
}