package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

public final class OrOperator extends Node {

  public OrOperator(Node child1, Node child2) {
    super(child1, child2);
  }

  boolean boolMode;

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
    Type t0 = children[0].returnType();
    if (t0 != Types.BOOLEAN && t0 != Types.NUMBER) {
      throw new IllegalArgumentException("First argument must be number or boolean instead of " + t0);
    }
    Type t1 = children[1].returnType();
    if (t1 != Types.BOOLEAN && t1 != Types.NUMBER) {
      throw new IllegalArgumentException("Second argument must be number or boolean instead of " + t1);
    }
    boolMode = children[0].returnType() == Types.BOOLEAN || children[1].returnType() == Types.BOOLEAN;
  }

  public Object eval(EvaluationContext evaluationContext) {
    if (boolMode) {
      return evalChildToBoolean(evaluationContext, 0) ? Boolean.TRUE : evalChildToBoolean(evaluationContext, 1);
    }
    return (double) (evalChildToInt(evaluationContext, 0) & evalChildToInt(evaluationContext, 1));
  }

  @Override
  public Type returnType() {
    return children[0].returnType();
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
    children[0].toString(asb, errors);
    appendLinked(asb, " OR ", errors);
    children[1].toString(asb, errors);
  }
}
