package org.kobjects.asde.lang.classifier;


import org.kobjects.asde.lang.property.Property;
import org.kobjects.asde.lang.property.PropertyDescriptor;
import org.kobjects.asde.lang.type.Typed;

public class Instance implements Typed {
  final Classifier classifier;
  final Object[] properties;

  public Instance(Classifier classifier, Object[] properties) {
    this.classifier = classifier;
    this.properties = properties;
  }

  public Classifier getType() { return classifier; }
}
