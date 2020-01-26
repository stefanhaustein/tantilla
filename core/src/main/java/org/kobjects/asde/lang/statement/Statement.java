package org.kobjects.asde.lang.statement;


import org.kobjects.asde.lang.function.Block;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.function.Types;
import org.kobjects.typesystem.Type;

public abstract class Statement extends Node {

  public Block block;

  Statement(Node... children) {
    super(children);
  }

  @Override
  public final Type returnType() {
    return Types.VOID;
  }

  public int getIndent() {
    if (block == null) {
      return 0;
    }
    return block.getDepth() - (closesBlock() ? 1 : 0);
  }


  public boolean closesBlock() {
    return false;
  }

  @Override
  public void resolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
    block = resolutionContext.getCurrentBlock();
    super.resolve(resolutionContext, parent, line, index);
  }

}
