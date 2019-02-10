package org.kobjects.asde.lang;

import org.kobjects.asde.lang.Interpreter;
import org.kobjects.typesystem.Type;

public interface ResolvedSymbol {
    Object get(Interpreter interpreter);
    void set(Interpreter interpreter, Object value);
    Type getType();
    boolean isConstant();
}
