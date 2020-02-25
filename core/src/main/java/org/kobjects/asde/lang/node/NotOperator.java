package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;

public class NotOperator extends Node {

  boolean boolMode;

  public NotOperator(Node child) {
    super(child);
  }

  @Override
  protected void onResolve(ValidationContext resolutionContext, int line) {
    boolean boolMode = Types.BOOL == children[0].returnType();
    if (!boolMode && Types.FLOAT != children[0].returnType()) {
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
