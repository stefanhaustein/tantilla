package org.kobjects.asde.lang.classifier;


import org.kobjects.asde.lang.property.PropertyDescriptor;
import org.kobjects.asde.lang.type.Type;

import java.util.Collection;

public interface Classifier extends Type {
  PropertyDescriptor getPropertyDescriptor(String name);

  Collection<? extends PropertyDescriptor> getPropertyDescriptors();

  CharSequence getDocumentation();
}

