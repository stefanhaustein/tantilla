package org.kobjects.typesystem;

import java.util.TreeMap;

public class InstanceTypeImpl extends TypeImpl implements InstanceType {

  TreeMap<String, PropertyDescriptor> propertyDescriptors = new TreeMap<>();

  public InstanceTypeImpl(String name, PropertyDescriptor... properties) {
    super(name);
    for (PropertyDescriptor property : properties) {
        this.propertyDescriptors.put(property.name(), property);
    }
  }

  public PropertyDescriptor getPropertyDescriptor(String name) {
        return propertyDescriptors.get(name);
    }
}
