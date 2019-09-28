package org.kobjects.asde.lang.statement;


import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.typesystem.Type;

public abstract class Statement extends Node {

  Statement(Node... children) {
    super(children);
  }

  @Override
  public final Type returnType() {
    return Types.VOID;
  }
}
