package org.kobjects.asde.lang.type;

public class AwaitableType implements Type {
  private final Type wrapped;

  public AwaitableType(Type wrapped) {
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


