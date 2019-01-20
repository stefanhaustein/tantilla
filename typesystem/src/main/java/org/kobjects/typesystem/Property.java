package org.kobjects.typesystem;

import java.util.ArrayList;

public abstract class Property<T> {
    ArrayList<PropertyChangeListener<T>> listeners;

    /**
     * Returns true if the value changed.
     */
    public final boolean set(T newValue) {
        if (setImpl(newValue) && listeners != null) {
            for (PropertyChangeListener<T> listener : listeners) {
                listener.propertyChanged(this, newValue);
            }
        }
        return true;
    }

    public abstract boolean setImpl(T t);
    public abstract T get();

    public synchronized void addListener(PropertyChangeListener<T> listener) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        listeners.add(listener);
    }
}
