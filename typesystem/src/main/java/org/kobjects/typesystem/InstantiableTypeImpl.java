package org.kobjects.typesystem;

public abstract class InstantiableTypeImpl extends InstanceTypeImpl implements InstantiableType {
  public InstantiableTypeImpl(String name, PropertyDescriptor... properties) {
    super(name, properties);
  }

  @Override
  public abstract Instance createInstance();
}
