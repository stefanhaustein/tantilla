package org.kobjects.typesystem;

public class MetaType implements Type {
  private final Type wrapped;

  public MetaType(Type wrapped) {
    this.wrapped = wrapped;
  }

  @Override
  public Type getType() {
    return new MetaType(this);
  }

  public Type getWrapped() {
    return wrapped;
  }

  @Override
  public boolean hasDefaultValue() {
    return false;
  }

  @Override
  public Object getDefaultValue() {
    throw new UnsupportedOperationException();
  }
}
