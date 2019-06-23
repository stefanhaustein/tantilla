package org.kobjects.typesystem;

public interface InstantiableType extends InstanceType {
  Instance createInstance();
}
