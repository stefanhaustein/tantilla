package org.kobjects.asde.library.ui;

import org.kobjects.typesystem.EnumLiteral;
import org.kobjects.typesystem.EnumType;

public enum EdgeMode implements EnumLiteral {

  NONE(SpriteAdapter.EDGE_MODE), WRAP(SpriteAdapter.EDGE_MODE), BOUNCE(SpriteAdapter.EDGE_MODE);

  private final EnumType type;

  @Override
  public EnumType getType() {
    return type;
  }

  EdgeMode(EnumType type) {
    this.type = type;
  }
}
