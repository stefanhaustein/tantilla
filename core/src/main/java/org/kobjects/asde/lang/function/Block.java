package org.kobjects.asde.lang.function;

import org.kobjects.asde.lang.statement.BlockStatement;

import java.util.HashMap;

public class Block {
  final Block parent;
  public final BlockStatement startStatement;
  final int startLine;
  final HashMap<String, LocalSymbol> localSymbols = new HashMap<>();

  Block(Block parent, BlockStatement startStatement, int startLine) {
    this.parent = parent;
    this.startStatement = startStatement;
    this.startLine = startLine;
  }

  LocalSymbol get(String name) {
    LocalSymbol result = localSymbols.get(name);
    return (result == null && parent != null) ? parent.get(name) : result;
  }

  public int getDepth() {
    return parent == null ? 0 : 1 + parent.getDepth();
  }
}
