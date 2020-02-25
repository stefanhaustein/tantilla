package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;

public class NegOperator extends Node {

  public NegOperator(Node child) {
    super(child);
  }

  @Override
  protected void onResolve(ValidationContext resolutionContext, int line) {
    if (children[0].returnType() != Types.FLOAT && children[0].returnType() != Types.BOOL) {
      throw new RuntimeException("Number argument expected for negation.");
    }
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    return evalDouble(evaluationContext);
  }

  @Override
  public double evalDouble(EvaluationContext evaluationContext) {
    return -children[0].evalDouble(evaluationContext);
  }

  @Override
  public Type returnType() {
    return Types.FLOAT;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb,"-", errors);
    children[0].toString(asb, errors, preferAscii);
  }
}
