package org.kobjects.asde.lang.type;

public interface Type extends Typed {
   Type[] EMPTY_ARRAY = new Type[0];

   boolean hasDefaultValue();

   Object getDefaultValue();

   default boolean isAssignableFrom(Type other) {
      return this.equals(other);
   }
}
