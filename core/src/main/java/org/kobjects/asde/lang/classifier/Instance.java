package org.kobjects.asde.lang.classifier;


import org.kobjects.asde.lang.property.Property;
import org.kobjects.asde.lang.property.PropertyDescriptor;
import org.kobjects.asde.lang.type.Typed;

public abstract class Instance implements Typed {
  private final Classifier classifier;

  public Instance(Classifier classifier) {
        this.classifier = classifier;
    }

  public abstract Property getProperty(PropertyDescriptor property);

  public Classifier getType() { return classifier; }
}
