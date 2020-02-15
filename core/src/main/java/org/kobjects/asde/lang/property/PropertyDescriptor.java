package org.kobjects.asde.lang.property;

import org.kobjects.asde.lang.classifier.Instance;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;

public interface PropertyDescriptor {
  String getName();
  Type getType();

  Object get(EvaluationContext context, Object instance);

  void set(EvaluationContext context, Object instance, Object value);


  default boolean isConstant() {
    return true;
  }
}
