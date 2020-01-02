package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.asde.lang.program.GlobalSymbol;
import org.kobjects.asde.lang.classifier.InstantiableType;

import java.util.Map;

public class New extends Node {
    final String name;
    InstantiableType instantiableType;

    public New(String name) {
        this.name = name;

    }

    @Override
    protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
        GlobalSymbol symbol = resolutionContext.program.getSymbol(name);
        if (symbol == null) {
            throw new RuntimeException("'" + name + "' is not defined");
        }
        symbol.validate(resolutionContext.programValidationContext);
        Object value = symbol.getValue();
        if (!(value instanceof InstantiableType)) {
            throw new RuntimeException("'" + name + "' is not an instantiable type.");
        }
        instantiableType = (InstantiableType) value;
    }

    @Override
    public Object eval(EvaluationContext evaluationContext) {
        return instantiableType.createInstance(evaluationContext);
    }

    @Override
    public InstantiableType returnType() {
        return instantiableType;
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
        appendLinked(asb, "new " + name, errors);
    }
}
