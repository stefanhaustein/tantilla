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
        GlobalSymbol symbol = interpreter.control.program.getSymbol(name);
        return symbol == null ? null : symbol.value;
    }

    @Override
    public void set(Interpreter interpreter, Object value) {
        interpreter.control.program.setValue(mode == ResolutionContext.ResolutionMode.SHELL
                ? GlobalSymbol.Scope.PERSISTENT : GlobalSymbol.Scope.TRANSIENT, name, value);
    }

    @Override
    public Type getType() {
        return null;
    }
}
