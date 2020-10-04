package org.kobjects.asde.lang.statement;


import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.expression.ExpressionNode;
import org.kobjects.asde.lang.runtime.EvaluationContext;

/**
 * A statement that starts a new block.
 */
public abstract class BlockStatement extends Statement {


  BlockStatement(ExpressionNode... children) {
    super(children);
  }

  public abstract void onResolveEnd(ValidationContext resolutionContext, EndStatement endStatement, int line);
  abstract void evalEnd(EvaluationContext context);
}
