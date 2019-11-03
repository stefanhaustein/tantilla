package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class NegOperator extends Node {

  public NegOperator(Node child) {
    super(child);
  }

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
    if (children[0].returnType() != Types.NUMBER && children[0].returnType() != Types.BOOLEAN) {
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
    return Types.NUMBER;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb,"-", errors);
    children[0].toString(asb, errors, preferAscii);
  }
}
