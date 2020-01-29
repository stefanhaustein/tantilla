package org.kobjects.typesystem;


public abstract class Instance implements Typed {
  private final InstanceType instanceType;

  public Instance(InstanceType instanceType) {
        this.instanceType = instanceType;
    }

  public abstract Property getProperty(PropertyDescriptor property);

  public InstanceType getType() { return instanceType; }
}
