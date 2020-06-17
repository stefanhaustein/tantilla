package org.kobjects.asde.lang.node;

import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.type.Types;

import java.util.Map;

public class BinaryNotOperator extends Node {

  public BinaryNotOperator(Node child) {
    super(child);
  }

  @Override
  protected void onResolve(ValidationContext resolutionContext, int line) {
    if (Types.FLOAT != children[0].returnType()) {
      throw new RuntimeException("Number parameter expected.");
    }
  }

  public Object eval(EvaluationContext evaluationContext) {
    return evalDouble(evaluationContext);
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
    appendLinked(asb,"~", errors);
    children[0].toString(asb, errors, preferAscii);
  }
}
