package org.kobjects.asde.lang.classifier;


import org.kobjects.asde.lang.type.Type;

import java.util.Collection;

/**
 * Seems to make sense to keep this as an interface as long as we don't know what to do about
 * generics (List).
 */
public interface Classifier extends Type {
  Property getProperty(String name);

  Collection<? extends Property> getAllProperties();

  void putProperty(Property property);

  CharSequence getDocumentation();

  default void remove(Property property) {
    throw new UnsupportedOperationException();
  }
}

