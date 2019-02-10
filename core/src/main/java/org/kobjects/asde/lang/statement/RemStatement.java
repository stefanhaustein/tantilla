package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class RemStatement extends Node {

    private final String comment;

    public RemStatement(String comment) {
        this.comment = comment;
    }

    @Override
    protected void onResolve(FunctionValidationContext resolutionContext, int line, int index) {
        // Nothing to do here.
    }

    @Override
    public Object eval(Interpreter interpreter) {
        return null;
    }

    @Override
    public Type returnType() {
        return Types.VOID;
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
        appendLinked(asb, "REM ", errors);
        asb.append(comment);
    }
}
