package org.kobjects.asde.lang;

import org.kobjects.typesystem.Type;

public interface ResolvedSymbol {
    Object get(EvaluationContext evaluationContext);
    void set(EvaluationContext evaluationContext, Object value);
    Type getType();
    boolean isConstant();
}
