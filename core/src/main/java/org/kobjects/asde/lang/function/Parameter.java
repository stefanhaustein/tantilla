package org.kobjects.asde.lang.function;

import org.kobjects.asde.lang.expression.ExpressionNode;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;

public class Parameter {
    public static final Parameter[] EMPTY_ARRAY = new Parameter[0];

    private final String name;
    private final Type explicitType;
    private final ExpressionNode defaultValueExpression;
    private Type resolvedType;

    private Parameter(String name, Type type) {
        this.name = name;
        this.explicitType = type;
        defaultValueExpression = null;
    }

    private Parameter(String name, ExpressionNode defaultValueExpression) {
        this.name = name;
        this.explicitType = null;
        this.defaultValueExpression = defaultValueExpression;
    }

    public static Parameter create(String name, Type type) {
        return new Parameter(name, type);
    }

    public static Parameter create(String name, ExpressionNode defaultValueExpression) {
        return new Parameter(name, defaultValueExpression);
    }

    public String getName() {
        return name;
    }

    public Type getExplicitType() {
        return explicitType != null ? explicitType : resolvedType;
    }

    public boolean hasDefaultValue() {
        return defaultValueExpression != null;
    }

    public ExpressionNode getDefaultValueExpression() {
        return defaultValueExpression;
    }

    public String toString() {
        return name + ": " + explicitType;
    }

    public void resolve(ValidationContext validationContext) {
        if (defaultValueExpression != null) {
            resolvedType = defaultValueExpression.resolveWasm(new WasmExpressionBuilder(), validationContext, 0);
        }
    }
}
