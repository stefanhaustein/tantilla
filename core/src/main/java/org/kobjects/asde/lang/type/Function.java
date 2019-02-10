package org.kobjects.asde.lang.type;

import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Typed;

public interface Function extends Typed {

    @Override
    FunctionType getType();

    /**
     * Calls this function with the given number of parameters on the stack.
     */
    Object call(EvaluationContext evaluationContext, int paramCount);
}
