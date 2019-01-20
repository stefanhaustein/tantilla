package org.kobjects.typesystem;

public interface PropertyChangeListener<T> {
    void propertyChanged(Property<T> property, T newValue);
}
