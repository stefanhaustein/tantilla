package org.kobjects.asde.lang.statement;


import org.kobjects.asde.lang.function.PropertyValidationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.runtime.EvaluationContext;

/**
 * A statement that starts a new block.
 */
public abstract class BlockStatement extends Statement {


  BlockStatement(Node... children) {
    super(children);
  }

  public abstract void onResolveEnd(PropertyValidationContext resolutionContext, EndStatement endStatement, int line);
  abstract void evalEnd(EvaluationContext context);
}
