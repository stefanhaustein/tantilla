package org.kobjects.asde.lang.function;

import org.kobjects.asde.lang.node.Assignable;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;

public class LocalSymbol implements Assignable {
    public final int index;
    private final Type type;
    private final boolean mutable;

    public LocalSymbol(int index, Type type, boolean mutable) {
        this.index = index;
        this.type = type;
        if (type == null) {
            throw new NullPointerException("type must not be null");
        }
        this.mutable = mutable;
    }

    public Object get(EvaluationContext evaluationContext) {
        return evaluationContext.getLocal(index);
    }

    @Override
    public void set(EvaluationContext evaluationContext, Object value) {
        evaluationContext.setLocal(index, value);
    }

    public Type getType() {
        return type;
    }

    public boolean isMutable() {
        return mutable;
    }
}
