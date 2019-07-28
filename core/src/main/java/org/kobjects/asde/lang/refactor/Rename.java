package org.kobjects.asde.lang.refactor;

import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.StaticSymbol;
import org.kobjects.asde.lang.node.Identifier;
import org.kobjects.asde.lang.node.Path;
import org.kobjects.asde.lang.node.SymbolNode;
import org.kobjects.asde.lang.node.Visitor;


public class Rename extends Visitor {

  private final StaticSymbol symbol;
  private final String oldName;
  private final String newName;


  public Rename(StaticSymbol symbol, String oldName, String newName) {
    this.symbol = symbol;
    this.oldName = oldName;
    this.newName = newName;
  }

  @Override
  public void visitIdentifier(Identifier identifier) {
    processSymbolNode(identifier);
  }

  @Override
  public void visitPath(Path path) {
    processSymbolNode(path);
  }

  void processSymbolNode(SymbolNode symbolNode) {
    if (symbolNode.matches(symbol, oldName)) {
      symbolNode.setName(newName);
    }
  }


  @Override
  public void visitProgram(Program program) {
    super.visitProgram(program);
    program.notifyProgramChanged();
  }
}
