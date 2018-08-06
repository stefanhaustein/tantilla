package org.kobjects.asde.lang;

import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Typed;

public interface Function extends Typed {

    FunctionType getType();

    Object eval(Interpreter interpreter, Object[] args);
}
