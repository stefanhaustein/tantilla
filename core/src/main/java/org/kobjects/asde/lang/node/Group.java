package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.parser.ResolutionContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class Group extends Node {
    public Group(Node child) {
        super(child);
    }

    @Override
    protected void onResolve(ResolutionContext resolutionContext, int line, int index) {
        // Nothing to do here.
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
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
        asb.append('(');

        children[0].toString(asb, errors);
        asb.append(')');
    }
}
