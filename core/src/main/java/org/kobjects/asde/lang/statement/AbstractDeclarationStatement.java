package org.kobjects.asde.lang.statement;

import org.kobjects.asde.lang.ResolvedSymbol;
import org.kobjects.asde.lang.StaticSymbol;
import org.kobjects.asde.lang.node.Node;

public abstract class AbstractDeclarationStatement extends Statement {

  String varName;
  ResolvedSymbol resolved;

  AbstractDeclarationStatement(String varName, Node... children) {
    super(children);
    this.varName = varName;
  }


  @Override
  public void rename(StaticSymbol symbol, String oldName, String newName) {
    if (symbol == resolved && oldName.equals(varName)) {
      varName = newName;
    }
  }

  public String getVarName() {
    return varName;
  }
}
