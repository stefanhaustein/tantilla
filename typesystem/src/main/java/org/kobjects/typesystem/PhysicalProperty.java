package org.kobjects.typesystem;

public class PhysicalProperty<T> extends Property<T> {

    T value;

    public PhysicalProperty(T initialValue) {
        value = initialValue;
    }

    @Override
    public boolean set(T newValue) {
        if (value.equals(newValue)) {
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
