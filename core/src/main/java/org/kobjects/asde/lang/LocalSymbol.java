package org.kobjects.asde.lang;

import org.kobjects.typesystem.Type;

public class LocalSymbol implements ResolvedSymbol {
    private final int index;
    private final Type type;
    private final boolean constant;

    public LocalSymbol(int index, Type type, boolean constant) {
        this.index = index;
        this.type = type;
        this.constant = constant;
    }

    @Override
    public Object get(EvaluationContext evaluationContext) {
        return evaluationContext.getLocal(index);
    }

    @Override
    public void set(EvaluationContext evaluationContext, Object value) {
        evaluationContext.setLocal(index, value);
    }

    @Override
    public Type getType() {
        return type;
    }

    public boolean isConstant() {
        return constant;
    }
}
