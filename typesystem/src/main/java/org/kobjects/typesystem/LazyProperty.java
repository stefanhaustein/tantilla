package org.kobjects.typesystem;

public abstract class LazyProperty<T> extends Property<T> {
  private T value;
  private boolean valid;

  @Override
  public boolean setImpl(T t) {
    throw new UnsupportedOperationException();
  }

  public boolean set(T newValue) {
    if (setImpl(newValue)) {
      value = newValue;
      valid = true;
      notifyChanged();
      return true;
    }
    return false;
  }


  @Override
  public synchronized T get() {
    if (!valid) {
      value = compute();
      valid = true;
    }
    return value;
  }

  public synchronized void invalidate() {
    T oldValue = value;
    valid = false;
    value = null;
    if (listeners != null && listeners.size() > 0 && !get().equals(oldValue)) {
      notifyChanged();
    }
  }


  @Override
  public void addListener(PropertyChangeListener listener) {
    super.addListener(listener);
    if (!valid && listeners.size() == 1) {
      invalidate();
    }
  }

  protected abstract T compute();
}
