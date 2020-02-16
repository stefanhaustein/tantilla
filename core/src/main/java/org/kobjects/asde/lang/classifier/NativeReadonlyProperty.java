package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;

public abstract class NativeReadonlyProperty extends NativeProperty {

  public NativeReadonlyProperty(String name, String description, Type type) {
    super(name, description, type);
  }

  @Override
  public void set(EvaluationContext context, Object instance, Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isConstant() {
    return true;
  }
}
