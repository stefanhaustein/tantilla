package org.kobjects.typesystem;

public interface EnumLiteral extends Typed {

  @Override
  EnumType getType();

  String name();
}
