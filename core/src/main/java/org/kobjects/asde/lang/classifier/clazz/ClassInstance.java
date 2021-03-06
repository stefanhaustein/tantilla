package org.kobjects.asde.lang.classifier.clazz;


import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.type.Typed;

public class ClassInstance implements Typed {
  final Classifier classifier;
  final Object[] properties;

  public ClassInstance(Classifier classifier, Object[] properties) {
    this.classifier = classifier;
    this.properties = properties;
  }

  public Classifier getType() { return classifier; }
}
