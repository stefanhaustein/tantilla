package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.node.AssignableNode;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.function.PropertyValidationContext;

import java.util.Map;

public class AssignStatement extends Statement {
  public AssignStatement(Node target, Node value) throws Exception {
    super(target, value);
    if (!(target instanceof AssignableNode)) {
      throw new Exception("Assignment target is not assignable.");
    }
  }

  @Override
  public boolean resolve(PropertyValidationContext resolutionContext, int line) {
    block = resolutionContext.getCurrentBlock();
    if (!children[1].resolve(resolutionContext, line)) {
      return false;
    }
    try {
      // May fail if resolve above has failed.
      ((AssignableNode) children[0]).resolveForAssignment(resolutionContext, children[1].returnType(), line);
      onResolve(resolutionContext, line);
    } catch (Exception e) {
      resolutionContext.addError(this, e);
    }
    return true;
   }


  @Override
  protected void onResolve(PropertyValidationContext resolutionContext, int line) {
    if (((AssignableNode) children[0]).isConstant()) {
      throw new RuntimeException("Cannot assign to a constant.");
    }
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    ((AssignableNode) children[0]).set(evaluationContext, children[1].eval(evaluationContext));
    return null;
  }


  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    children[0].toString(asb, errors, preferAscii);
    appendLinked(asb, " = ", errors);
    children[1].toString(asb, errors, preferAscii);
  }
}
