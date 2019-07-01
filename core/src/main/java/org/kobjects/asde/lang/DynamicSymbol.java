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
    public Object get(EvaluationContext evaluationContext) {
        GlobalSymbol symbol = evaluationContext.control.program.getSymbol(name);
        return symbol == null ? null : symbol.value;
    }

    @Override
    public void set(EvaluationContext evaluationContext, Object value) {
        evaluationContext.control.program.setValue(mode == FunctionValidationContext.ResolutionMode.INTERACTIVE
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
