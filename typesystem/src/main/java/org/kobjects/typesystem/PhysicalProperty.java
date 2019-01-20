package org.kobjects.typesystem;

public class PhysicalProperty<T> extends Property<T> {
    private T value;

    public PhysicalProperty(T initialValue) {
        value = initialValue;
    }

    @Override
    public boolean setImpl(T newValue) {
        if (newValue.equals(value)) {
            return false;
        }
        this.value = newValue;
        return true;
    }

    @Override
    public T get() {
        return value;
    }
}
