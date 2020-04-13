package org.kobjects.asde.lang.type;

public interface Type extends Typed {
   Type[] EMPTY_ARRAY = new Type[0];

   boolean hasDefaultValue();

   Object getDefaultValue();

   default boolean supportsChangeListeners() {
      return false;
   }

   default void addChangeListener(Object instance, Runnable changeListener) {
      throw new UnsupportedOperationException();
   }
}
