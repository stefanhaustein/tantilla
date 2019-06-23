package org.kobjects.typesystem;


public interface InstanceType extends Type {
  PropertyDescriptor getPropertyDescriptor(String name);
}
