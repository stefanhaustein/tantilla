package org.kobjects.typesystem;

public abstract class LazyProperty<T> extends Property<T> {
    private T value;
    private boolean valid;

    @Override
    public boolean setImpl(T t) {
        throw new UnsupportedOperationException();
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
        if (listeners != null && listeners.size() != 0) {
            T newValue = compute();
            if (!valid || !newValue.equals(value)) {
                value = newValue;
                valid = true;
                notifyChanged();
            }
        } else {
            valid = false;
            value = null;
        }
    }

    protected abstract T compute();
}
