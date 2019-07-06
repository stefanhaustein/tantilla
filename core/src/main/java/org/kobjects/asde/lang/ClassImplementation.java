package org.kobjects.asde.lang;

import org.kobjects.typesystem.InstanceType;
import org.kobjects.typesystem.MetaType;
import org.kobjects.typesystem.PropertyDescriptor;
import org.kobjects.typesystem.Type;

public class ClassImplementation implements InstanceType {

  String name;

  public ClassImplementation(String name) {
    this.name = name;
  }


  @Override
  public PropertyDescriptor getPropertyDescriptor(String name) {
    throw new RuntimeException("NYI");
  }

  @Override
  public Type getType() {
    return new MetaType(this);
  }
}
