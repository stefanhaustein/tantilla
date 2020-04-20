package org.kobjects.asde.lang.classifier.builtin;

import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;

public abstract class NativeReadonlyProperty extends NativeProperty {

  public NativeReadonlyProperty(Classifier owner, String name, String description, Type type) {
    super(owner, name, description, type);
  }

  @Override
  public void set(EvaluationContext context, Object instance, Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isMutable() {
    return false;
  }

}
