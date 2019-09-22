package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.type.Function;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class VoidStatement extends Node {
    public VoidStatement(Node expression) {
        super(expression);
    }

    @Override
    protected void onResolve(FunctionValidationContext resolutionContext, int line, int index) {
        // Nothing to do here.
    }

    @Override
    public Object eval(EvaluationContext evaluationContext) {
        Object result = children[0].eval(evaluationContext);
        if (result instanceof Function) {
            Function function = (Function) result;
            evaluationContext.ensureExtraStackSpace(function.getLocalVariableCount());
            function.call(evaluationContext, 0);
        }
        return result;
    }

    @Override
    public Type returnType() {
        return null;
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
        int start = 0;
        children[0].toString(asb, errors);
        asb.annotate(start, asb.length(), errors.get(this));
    }
}
