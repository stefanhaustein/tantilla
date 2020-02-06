package org.kobjects.asde.lang.classifier;


import org.kobjects.asde.lang.property.Property;
import org.kobjects.asde.lang.property.PropertyDescriptor;
import org.kobjects.asde.lang.type.Typed;

public abstract class Instance implements Typed {
  private final InstanceType instanceType;

  public Instance(InstanceType instanceType) {
        this.instanceType = instanceType;
    }

  public abstract Property getProperty(PropertyDescriptor property);

  public InstanceType getType() { return instanceType; }
}
