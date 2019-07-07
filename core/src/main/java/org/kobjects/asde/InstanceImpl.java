package org.kobjects.asde;

import org.kobjects.asde.lang.ClassImplementation;
import org.kobjects.typesystem.Instance;
import org.kobjects.typesystem.PhysicalProperty;
import org.kobjects.typesystem.Property;
import org.kobjects.typesystem.PropertyDescriptor;

public class InstanceImpl extends Instance {
  final Property[] properties;

  public InstanceImpl(ClassImplementation clazz, Property[] properties) {
    super(clazz);
    this.properties = properties;
  }

  @Override
  public Property getProperty(PropertyDescriptor rawDescriptor) {
    ClassImplementation.ClassPropertyDescriptor descriptor = ((ClassImplementation.ClassPropertyDescriptor) rawDescriptor);

    int index = descriptor.getIndex();
    if (index != -1) {
      return properties[index];
    }
    throw new RuntimeException("Method binding NYI");
  }

}
