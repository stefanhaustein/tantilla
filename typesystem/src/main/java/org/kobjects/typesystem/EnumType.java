package org.kobjects.typesystem;

import java.util.Arrays;

public class EnumType implements Type, Typed {
  public final EnumLiteral[] literals;

  public EnumType(EnumLiteral... literals) {
    this.literals = literals;
  }

  @Override
  public Type getType() {
    return new MetaType(this);
  }

  public EnumLiteral getLiteral(String pathName) {
    for (EnumLiteral literal : literals) {
      if (pathName.equals(literal.name())) {
        return literal;
      }
    }
    throw new RuntimeException("Enum literal named '" + pathName + "' not found. Valid literals are: " + Arrays.toString(literals));
  }
}
