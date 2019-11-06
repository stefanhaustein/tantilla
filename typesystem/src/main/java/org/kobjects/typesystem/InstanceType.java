package org.kobjects.typesystem;


import java.util.Collection;

public interface InstanceType extends Type {
  PropertyDescriptor getPropertyDescriptor(String name);

  Collection<? extends PropertyDescriptor> getPropertyDescriptors();
}
