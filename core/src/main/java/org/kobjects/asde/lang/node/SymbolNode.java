package org.kobjects.asde.lang.node;


import org.kobjects.asde.lang.StaticSymbol;

public abstract class SymbolNode extends AssignableNode {

  SymbolNode(Node... children) {
    super(children);
  }

  public abstract boolean matches(StaticSymbol symbol, String oldName);

  public abstract void setName(String newName);

}
