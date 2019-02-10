package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class NotOperator extends Node {

  public NotOperator(Node child) {
    super(child);
  }

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, int line, int index) {
    if (!Types.match(children[0].returnType(), Types.BOOLEAN)
            && !Types.match(children[0].returnType(), Types.NUMBER)) {
      throw new RuntimeException("Boolean or Number parameter expected.");
    }
  }

  public Object eval(EvaluationContext evaluationContext) {
    Object lVal = children[0].eval(evaluationContext);
    if (lVal instanceof Boolean) {
      return !((Boolean) lVal);
    }
    if (lVal instanceof Double) {
      return ~((Double) lVal).intValue();
    }
    throw new EvaluationException(children[0], "Boolean or Number expected for NOT.");
  }



  @Override
  public Type returnType() {
    return children[0].returnType() == Types.BOOLEAN || children[0].returnType() == Types.BOOLEAN
            ? children[0].returnType() : null;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
    appendLinked(asb,"NOT ", errors);
    children[0].toString(asb, errors);
  }
}
