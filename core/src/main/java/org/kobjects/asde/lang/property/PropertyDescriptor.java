package org.kobjects.asde.lang.property;

import org.kobjects.asde.lang.classifier.Instance;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;

public interface PropertyDescriptor {
  String name();
  Type type();

  default Object get(EvaluationContext context, Object instance) {
    return ((Instance) instance).getProperty(this).get();
  }

  default void set(EvaluationContext context, Object instance, Object value) {
    if (!(instance instanceof Instance)) {
      throw new RuntimeException("Object " + instance + " of class " + instance.getClass() + " is not an instance. PropertyDescriptor " + name() + " type " + type() + " class " + getClass());
    }
    ((Instance) instance).getProperty(this).set(value);
  }

  default void addListener(Object instance, PropertyChangeListener listener) {
    ((Instance) instance).getProperty(this).addListener(listener);
  }
}
