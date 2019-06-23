package org.kobjects.typesystem;

import org.kobjects.typesystem.MetaType;
import org.kobjects.typesystem.Type;

public class TypeImpl implements Type {
  private final String name;

  public TypeImpl(String name) {
    this.name = name;
  }

  @Override
  public Type getType() {
    return new MetaType(this);
  }

  public String toString() {
    return name;
  }
}
