package org.kobjects.typesystem;

public interface Type extends Typed {
   Type[] EMPTY_ARRAY = new Type[0];

   boolean hasDefaultValue();

   Object getDefaultValue();
}
