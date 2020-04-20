package org.kobjects.asde.lang.classifier.builtin;

import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;

public abstract class NativeProperty implements Property {

  private final Classifier owner;
  private final String name;
  private final Type type;

  public NativeProperty(Classifier owner, String name, String description, Type type) {
    this.owner = owner;
    this.name = name;
    this.type = type;
  }

  @Override
  public Classifier getOwner() {
    return owner;
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
  public boolean isMutable() {
    return true;
  }

  @Override
  public boolean isInstanceField() {
    return true;
  }

  @Override
  public Object getStaticValue() {
    throw new UnsupportedOperationException("Not static.");
  }

}
