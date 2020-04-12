package org.kobjects.asde.lang.classifier;

public class AdapterInstance {
  final AdapterType adapterType;
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
