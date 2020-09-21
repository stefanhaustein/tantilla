package org.kobjects.asde.lang.function;

import org.kobjects.asde.lang.node.ExpressionNode;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.type.Type;

public class Parameter {
    public static final Parameter[] EMPTY_ARRAY = new Parameter[0];

    private final String name;
    private final Type type;
    private final ExpressionNode defaultValueExpression;

    private Parameter(String name, Type type) {
        this.name = name;
        this.type = type;
        defaultValueExpression = null;
    }

    private Parameter(String name, ExpressionNode defaultValueExpression) {
        this.name = name;
        this.type = null;
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

    public Type getType() {
        return type != null ? type : defaultValueExpression.returnType();
    }

    public ExpressionNode getDefaultValueExpression() {
        return defaultValueExpression;
    }

    public String toString() {
        return name + ": " + type;
    }
}
