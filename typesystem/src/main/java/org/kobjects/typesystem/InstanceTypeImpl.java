package org.kobjects.typesystem;

import java.util.TreeMap;

public class InstanceTypeImpl implements InstanceType {

  TreeMap<String, PropertyDescriptor> propertyDescriptors = new TreeMap<>();

  public InstanceTypeImpl(PropertyDescriptor... properties) {
    for (PropertyDescriptor property : properties) {
        this.propertyDescriptors.put(property.name(), property);
    }
  }

  public PropertyDescriptor getPropertyDescriptor(String name) {
        return propertyDescriptors.get(name);
    }

  public Type getType() {
        return new MetaType(this);
    }
}
