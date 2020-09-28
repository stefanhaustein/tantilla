package org.kobjects.asde.lang.statement;


import org.kobjects.asde.lang.function.Block;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.node.ExpressionNode;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.type.Type;

public abstract class Statement extends Node {

  public Block block;

  Statement(ExpressionNode... children) {
    super(children);
  }

  @Override
  protected void onResolve(ValidationContext resolutionContext, int line) {
    throw new UnsupportedOperationException();
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
  public boolean resolve(ValidationContext resolutionContext, int line) {
    block = resolutionContext.getCurrentBlock();
    resolveImpl(resolutionContext, line);
    return true;
  }

  protected void resolveImpl(ValidationContext validationContext, int line) {
    super.resolve(validationContext, line);
  }


}
