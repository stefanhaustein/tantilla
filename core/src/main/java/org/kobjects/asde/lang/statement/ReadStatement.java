package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.asde.lang.node.AssignableNode;
import org.kobjects.asde.lang.node.Node;

import java.util.Map;

public class ReadStatement extends Statement {

  public ReadStatement(Node[] children) {
    super(children);
  }

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
    for (Node node : children) {
      if (!(node instanceof AssignableNode) || !((AssignableNode) node).isAssignable()) {
        resolutionContext.addError(node, new RuntimeException("Not assignable"));
      }
      ((AssignableNode) node).resolveForAssignment(resolutionContext, parent, node.returnType(), line, index);
    }
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    for (int i = 0; i < children.length; i++) {
      int[] dataPosition = evaluationContext.getDataPosition();
      while (evaluationContext.dataStatement == null
          || dataPosition[2] >= evaluationContext.dataStatement.children.length) {
        dataPosition[2] = 0;
        if (evaluationContext.dataStatement != null) {
          dataPosition[1]++;
        }
        evaluationContext.dataStatement = (LegacyStatement) evaluationContext.control.program.main.find((line, index, statement)->(statement instanceof LegacyStatement && ((LegacyStatement) statement).kind == LegacyStatement.Kind.DATA), dataPosition);
        if (evaluationContext.dataStatement == null) {
          throw new RuntimeException("Out of data.");
        }
      }
      ((AssignableNode) children[i]).set(evaluationContext, evaluationContext.dataStatement.children[dataPosition[2]++].eval(evaluationContext));
    }
    return null;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, "READ ", errors);
    children[0].toString(asb, errors, preferAscii);
    for (int i = 1; i < children.length; i++) {
      asb.append(", ");
      children[i].toString(asb, errors, preferAscii);
    }
  }
}
