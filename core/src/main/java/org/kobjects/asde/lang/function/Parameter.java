package org.kobjects.asde.lang.function;

import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.type.Type;

public class Parameter {
    public static final Parameter[] EMPTY_ARRAY = new Parameter[0];

    private final String name;
    private final Type type;
    private final Node defaultValueExpression;

    private Parameter(String name, Type type) {
        this.name = name;
        this.type = type;
        defaultValueExpression = null;
    }

    private Parameter(String name, Node defaultValueExpression) {
        this.name = name;
        this.type = null;
        this.defaultValueExpression = defaultValueExpression;
    }

    public static Parameter create(String name, Type type) {
        return new Parameter(name, type);
    }

    public static Parameter create(String name, Node defaultValueExpression) {
        return new Parameter(name, defaultValueExpression);
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type != null ? type : defaultValueExpression.returnType();
    }

    public Node getDefaultValueExpression() {
        return defaultValueExpression;
    }

    public String toString() {
        return name + ": " + type;
    }
}
