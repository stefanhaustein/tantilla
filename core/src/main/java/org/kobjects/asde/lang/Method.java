package org.kobjects.asde.lang;

import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Property;
import org.kobjects.typesystem.PropertyDescriptor;

public abstract class Method extends Property implements Function {
    final FunctionType type;

    public Method(FunctionType type) {
        this.type = type;
    }

    @Override
    public FunctionType getType() {
        return type;
    }

    @Override
    public boolean set(Object o) {
        throw new RuntimeException();
    }

    @Override
    public Object get() {
        return this;
    }
}
