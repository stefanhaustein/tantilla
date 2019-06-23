package org.kobjects.typesystem;

public abstract class InstantiableTypeImpl extends InstanceTypeImpl implements InstantiableType {
  @Override
  public abstract Instance createInstance();
}
