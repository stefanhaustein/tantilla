package org.kobjects.asde.lang.type;

public class TypeImpl implements Type {
  private final String name;
  private final Object defaultValue;

  public TypeImpl(String name, Object defaultValue) {
    this.name = name;
    this.defaultValue = defaultValue;
  }

  @Override
  public Type getType() {
    return new MetaType(this);
  }

  public String toString() {
    return name;
  }

  public Object getDefaultValue() {
    if (defaultValue == null) {
      throw new UnsupportedOperationException();
    }
    return defaultValue;
  }

  public boolean hasDefaultValue() {
    return defaultValue != null;
  }
}
