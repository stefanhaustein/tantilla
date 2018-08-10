package org.kobjects.asde.lang.symbol;

import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.parser.ResolutionContext;
import org.kobjects.typesystem.Type;

public class DynamicSymbol implements ResolvedSymbol {
    private final String name;
    private final ResolutionContext.ResolutionMode mode;

    public DynamicSymbol(String name, ResolutionContext.ResolutionMode mode) {
        this.name = name;
        this.mode = mode;
    }

    @Override
    public Object get(Interpreter interpreter) {
        GlobalSymbol symbol = interpreter.program.getSymbol(name);
        return symbol == null ? null : symbol.value;
    }

    @Override
    public void set(Interpreter interpreter, Object value) {
        GlobalSymbol symbol = interpreter.program.getSymbol(name);
        if (symbol == null) {
            symbol = new GlobalSymbol(mode == ResolutionContext.ResolutionMode.SHELL
                    ? GlobalSymbol.Scope.PERSISTENT : GlobalSymbol.Scope.TRANSIENT, value);
            interpreter.program.setSymbol(name, symbol);
        }
        // TODO: Check type match and assignability if the target is persistent and the mode is transient
        symbol.value = value;
    }

    @Override
    public Type getType() {
        return null;
    }
}
