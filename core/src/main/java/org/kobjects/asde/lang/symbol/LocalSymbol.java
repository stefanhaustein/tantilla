package org.kobjects.asde.lang.symbol;

import org.kobjects.asde.lang.Interpreter;
import org.kobjects.typesystem.Type;

public class LocalSymbol implements ResolvedSymbol {
    private final int index;
    private final Type type;

    public LocalSymbol(int index, Type type) {
        this.index = index;
        this.type = type;
    }

    @Override
    public Object get(Interpreter interpreter) {
        return interpreter.localStack.getLocal(index);
    }

    @Override
    public void set(Interpreter interpreter, Object value) {
        interpreter.localStack.setLocal(index, value);
    }

    @Override
    public Type getType() {
        return type;
    }
}
