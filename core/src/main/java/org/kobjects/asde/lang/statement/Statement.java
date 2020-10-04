package org.kobjects.asde.lang.statement;


import org.kobjects.asde.lang.function.Block;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.expression.ExpressionNode;
import org.kobjects.asde.lang.expression.Node;
import org.kobjects.asde.lang.runtime.EvaluationContext;

public abstract class Statement extends Node {

  public Block block;

  Statement(ExpressionNode... children) {
    super(children);
  }

  public abstract Object eval(EvaluationContext evaluationContext);

  public int getIndent() {
    if (block == null) {
      return 0;
    }
    return block.getDepth() - (closesBlock() ? 1 : 0);
  }


  public boolean closesBlock() {
    return false;
  }

  public final boolean resolve(ValidationContext resolutionContext, int line) {
    block = resolutionContext.getCurrentBlock();
    resolveImpl(resolutionContext, line);
    return true;
  }

  protected abstract void resolveImpl(ValidationContext validationContext, int line);


}
