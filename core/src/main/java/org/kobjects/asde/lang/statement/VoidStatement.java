package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.function.Callable;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.function.PropertyValidationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;

public class VoidStatement extends Statement {
  public VoidStatement(Node expression) {
    super(expression);
  }

  @Override
  protected void onResolve(PropertyValidationContext resolutionContext, int line) {
    Type returnType = children[0].returnType();
    if (returnType instanceof FunctionType) {
      returnType = ((FunctionType) returnType).getReturnType();
    }
    if (returnType != Types.VOID) {
      throw new RuntimeException("Function result typed "+ returnType + " ignored.");
    }
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    Object result = children[0].eval(evaluationContext);
    if (result instanceof Callable) {
      Callable function = (Callable) result;
      evaluationContext.ensureExtraStackSpace(function.getLocalVariableCount());
      function.call(evaluationContext, 0);
    }
    return result;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    int start = 0;
    children[0].toString(asb, errors, preferAscii);
    asb.annotate(start, asb.length(), errors.get(this));
  }
}
