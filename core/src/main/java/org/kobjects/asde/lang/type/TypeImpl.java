package org.kobjects.asde.lang.type;

import org.kobjects.typesystem.MetaType;
import org.kobjects.typesystem.Type;

public class TypeImpl implements Type {
  private final String name;

  TypeImpl(String name) {
    this.name = name;
  }

  @Override
  public Type getType() {
    return new MetaType(this);
  }
}
