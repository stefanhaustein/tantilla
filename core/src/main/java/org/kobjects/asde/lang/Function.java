package org.kobjects.asde.lang;

import org.kobjects.asde.lang.type.FunctionType;
import org.kobjects.asde.lang.type.Typed;

public interface Function extends Typed {

    FunctionType getType();

    Object eval(Interpreter interpreter, Object[] args);
}
