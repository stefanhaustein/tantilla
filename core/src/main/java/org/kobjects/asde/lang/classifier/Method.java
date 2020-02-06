package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.function.Function;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.property.Property;

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
    public boolean setImpl(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object get() {
        return this;
    }

}
