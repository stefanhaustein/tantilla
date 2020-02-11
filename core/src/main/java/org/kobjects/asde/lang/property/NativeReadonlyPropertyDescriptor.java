package org.kobjects.asde.lang.property;

import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;

public abstract class NativeReadonlyPropertyDescriptor extends NativePropertyDescriptor {

  public NativeReadonlyPropertyDescriptor(String name, String description, Type type) {
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
