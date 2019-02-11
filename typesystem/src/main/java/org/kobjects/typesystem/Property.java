package org.kobjects.typesystem;

import java.util.ArrayList;

public abstract class Property<T> {
    ArrayList<PropertyChangeListener> listeners;

    /**
     * Returns true if the value changed.
     */
    public final boolean set(T newValue) {
        if (setImpl(newValue)) {
            notifyChanged();
        }
        return true;
    }

    public abstract boolean setImpl(T t);
    public abstract T get();

    public void notifyChanged() {
                if (listeners != null) {
                    for (PropertyChangeListener listener : listeners) {
                        listener.propertyChanged(this);
                    }
                }


    }

    public synchronized void addListener(PropertyChangeListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        listeners.add(listener);
    }
}
