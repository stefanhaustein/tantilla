package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;

public abstract class NativePropertyDescriptor implements PropertyDescriptor {

  private final String name;
  private final Type type;

  public NativePropertyDescriptor(String name, String description, Type type) {
    this.name = name;
    this.type = type;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public abstract Object get(EvaluationContext context, Object instance);

  @Override
  public abstract void set(EvaluationContext context, Object instance, Object value);


  @Override
  public boolean isConstant() {
    return false;
  }
}
