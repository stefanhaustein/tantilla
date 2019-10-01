package org.kobjects.typesystem;

import java.util.Arrays;

public class EnumType implements Type, Typed {
  public final Object[] literals;

  public EnumType(Object... literals) {
    this.literals = literals;
  }

  @Override
  public Type getType() {
    return new MetaType(this);
  }

  public Object getLiteral(String name) {
    for (Object literal : literals) {
      if (name.equals(literal.toString())) {
        return literal;
      }
    }
    throw new RuntimeException("Enum literal named '" + name + "' not found. Valid literals are: " + Arrays.toString(literals));
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
