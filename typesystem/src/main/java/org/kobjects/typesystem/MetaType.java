package org.kobjects.typesystem;

public class MetaType implements Typed, Type {
    private final Type type;

    MetaType(Type type) {
        this.type = type;
    }

    @Override
    public Type getType() {
        return type;
    }
}