package org.kobjects.asde.lang.statement;

import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.node.TraitCast;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.node.AssignableNode;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;

public class AssignStatement extends Statement {

  Node resolvedTarget;

  public AssignStatement(Node target, Node value) throws Exception {
    super(target, value);
    if (!(target instanceof AssignableNode)) {
      throw new Exception("Assignment target is not assignable.");
    }
  }

  @Override
  public boolean resolve(ValidationContext resolutionContext, int line) {
    block = resolutionContext.getCurrentBlock();
    if (!children[1].resolve(resolutionContext, line)) {
      return false;
    }
    try {
      // May fail if resolve above has failed.
      Type expectedType = ((AssignableNode) children[0]).resolveForAssignment(resolutionContext, line);
      resolvedTarget = TraitCast.autoCast(children[1], expectedType, resolutionContext);
    } catch (Exception e) {
      resolutionContext.addError(this, e);
    }
    return true;
   }


  @Override
  protected void onResolve(ValidationContext resolutionContext, int line) {
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    ((AssignableNode) children[0]).set(evaluationContext, resolvedTarget.eval(evaluationContext));
    return null;
  }


  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    children[0].toString(asb, errors, preferAscii);
    appendLinked(asb, " = ", errors);
    children[1].toString(asb, errors, preferAscii);
  }
}
