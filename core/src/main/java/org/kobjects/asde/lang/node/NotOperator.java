package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.Types;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class NotOperator extends Node {

  boolean boolMode;

  public NotOperator(Node child) {
    super(child);
  }

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line) {
    boolean boolMode = Types.BOOLEAN == children[0].returnType();
    if (!boolMode && Types.NUMBER != children[0].returnType()) {
      throw new RuntimeException("Boolean or Number parameter expected.");
    }
  }

  public Object eval(EvaluationContext evaluationContext) {
    if (boolMode) {
      return evalBoolean(evaluationContext);
    }
    return evalDouble(evaluationContext);
  }

  public boolean evalBoolean(EvaluationContext evaluationContext) {
    return !children[0].evalBoolean(evaluationContext);
  }

  public double evalDouble(EvaluationContext evaluationContext) {
    return ~children[0].evalInt(evaluationContext);
  }

  public int evalInt(EvaluationContext evaluationContext) {
    return ~children[0].evalInt(evaluationContext);
  }

  @Override
  public Type returnType() {
    return children[0].returnType();
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb,"NOT ", errors);
    children[0].toString(asb, errors, preferAscii);
  }
}
