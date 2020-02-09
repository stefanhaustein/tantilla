package org.kobjects.asde.lang.type;

public interface ChangeListener<T> {
  void notifyChanged(T object);
}
