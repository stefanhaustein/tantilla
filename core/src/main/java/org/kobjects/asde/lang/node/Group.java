package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class Group extends Node {
    public Group(Node child) {
        super(child);
    }

    @Override
    protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
        // Nothing to do here.
    }

    @Override
    public Object eval(EvaluationContext evaluationContext) {
        return children[0].eval(evaluationContext);
    }

    @Override
    public Type returnType() {
        return children[0].returnType();
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
        appendLinked(asb, "(", errors);

        children[0].toString(asb, errors);
        appendLinked(asb, ")", errors);
    }
}
