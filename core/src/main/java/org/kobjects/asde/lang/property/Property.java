package org.kobjects.asde.lang.property;

import java.util.LinkedHashSet;

public abstract class Property<T> {
  LinkedHashSet<PropertyChangeListener> listeners;

  /**
   * Returns true if the value changed.
   */
  public boolean set(T newValue) {
    if (setImpl(newValue)) {
      notifyChanged();
      return true;
    }
    return false;
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
      listeners = new LinkedHashSet<>();
    }
    listeners.add(listener);
  }
}
