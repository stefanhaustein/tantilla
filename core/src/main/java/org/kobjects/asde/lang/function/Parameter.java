package org.kobjects.asde.lang.function;

import org.kobjects.asde.lang.type.Type;

public class Parameter {
    public static final Parameter[] EMPTY_ARRAY = new Parameter[0];
    public final String name;
    public final Type type;

    private Parameter(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public static Parameter create(String name, Type type) {
        return new Parameter(name, type);
    }

    public String toString() {
        return name + ": " + type;
    }
}
