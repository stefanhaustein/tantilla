package org.kobjects.asde.lang.type;

import org.kobjects.annotatedtext.AnnotatedString;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Typed;

public interface Function extends Typed, Callable {


    @Override
    FunctionType getType();

    /**
     * Calls this function with the given number of parameters on the stack.
     */
    @Override
    Object call(EvaluationContext evaluationContext, int paramCount);

    default int getLocalVariableCount() {
        return getType().getParameterCount();
    }

    default CharSequence getDocumentation() { return null; }
}
