package org.kobjects.asde.lang;

import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Typed;

public interface Function extends Typed {

    FunctionType getType();

    /**
     * Calls this function with the given number of parameters on the stack.
     */
    Object call(Interpreter interpreter, int paramCount);
}
