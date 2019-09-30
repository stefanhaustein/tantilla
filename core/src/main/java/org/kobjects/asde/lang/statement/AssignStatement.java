package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.node.AssignableNode;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.FunctionValidationContext;

import java.util.Map;

public class AssignStatement extends Statement {
  public AssignStatement(Node target, Node value) throws Exception {
    super(target, value);
    if (!(target instanceof AssignableNode)) {
      throw new Exception("Assignment target is not assignable.");
    }
  }

  @Override
  public void resolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
    children[1].resolve(resolutionContext, this, line, index);
    try {
      // May fail if resolve above has failed.
      ((AssignableNode) children[0]).resolveForAssignment(resolutionContext, this, children[1].returnType(), line, index);
      onResolve(resolutionContext, parent, line, index);
    } catch (Exception e) {

    }
   }


  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
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
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
    children[0].toString(asb, errors);
    appendLinked(asb, " = ", errors);
    children[1].toString(asb, errors);
  }
}
