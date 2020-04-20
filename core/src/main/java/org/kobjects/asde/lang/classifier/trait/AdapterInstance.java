package org.kobjects.asde.lang.classifier.trait;

import org.kobjects.asde.lang.classifier.clazz.ClassInstance;

public class AdapterInstance {
  public final AdapterType adapterType;
  public final ClassInstance instance;

  public AdapterInstance(AdapterType adapterType, ClassInstance classInstance) {
    if (adapterType == null) {
      throw new NullPointerException();
    }
    this.adapterType = adapterType;
    if (classInstance == null) {
      throw new NullPointerException();
    }
    this.instance = classInstance;
  }
}
