package org.kobjects.asde.lang;

import org.kobjects.typesystem.Type;

public class DynamicSymbol implements ResolvedSymbol {
    private final String name;
    private final FunctionValidationContext.ResolutionMode mode;

    public DynamicSymbol(String name, FunctionValidationContext.ResolutionMode mode) {
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
        interpreter.control.program.setValue(mode == FunctionValidationContext.ResolutionMode.SHELL
                ? GlobalSymbol.Scope.PERSISTENT : GlobalSymbol.Scope.TRANSIENT, name, value);
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public boolean isConstant() {
        return false;
    }
}
