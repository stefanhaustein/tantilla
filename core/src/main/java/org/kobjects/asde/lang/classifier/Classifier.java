package org.kobjects.asde.lang.classifier;


import org.kobjects.asde.lang.type.Type;

import java.util.Collection;

public interface Classifier extends Type {
  Property getPropertyDescriptor(String name);

  Collection<? extends Property> getAllProperties();

  CharSequence getDocumentation();
}

