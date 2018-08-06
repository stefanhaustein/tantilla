package org.kobjects.typesystem;

public abstract class Property<T> {

    /**
     * Returns true if the value changed.
     */
    public abstract boolean set(T t);
    public abstract T get();


}
