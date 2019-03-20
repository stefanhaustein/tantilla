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
        valid = false;
        value = null;
        if (listeners != null && listeners.size() != 0) {
            value = compute();
            valid = true;
            notifyChanged();
        }
    }

    protected abstract T compute();
}
