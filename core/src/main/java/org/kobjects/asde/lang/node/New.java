package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.asde.lang.GlobalSymbol;
import org.kobjects.asde.lang.type.InstantiableType;

import java.util.Map;

public class New extends Node {
    final String name;
    InstantiableType instantiableType;

    public New(Program program, String name) throws Exception {
        this.name = name;
        GlobalSymbol symbol = program.getSymbol(name);
        if (symbol == null) {
            throw new Exception("'" + name + "' is not defined");
        }
        Object value = symbol.getValue();
        if (!(value instanceof InstantiableType)) {
            throw new Exception("'" + name + "' is not an instantiable type.");
        }
        instantiableType = (InstantiableType) value;
    }

    @Override
    protected void onResolve(FunctionValidationContext resolutionContext, int line, int index) {
        // TODO
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
    public void toString(AnnotatedStringBuilder asb, Map<Node,Exception> errors) {
        appendLinked(asb, "new " + name, errors);
    }
}
