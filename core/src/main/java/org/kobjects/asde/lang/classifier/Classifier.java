package org.kobjects.asde.lang.classifier;


import org.kobjects.asde.lang.type.Type;

import java.util.Collection;

public interface Classifier extends Type {
  Property getProperty(String name);

  Collection<? extends Property> getAllProperties();

  void putProperty(Property property);

  CharSequence getDocumentation();
}

