package org.kobjects.asde.lang.statement;

import org.kobjects.asde.lang.node.ExpressionNode;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;
import org.kobjects.asde.lang.wasm.runtime.WasmExpression;
import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.exceptions.ExceptionWithReplacementPropolsal;
import org.kobjects.asde.lang.function.Callable;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;

public class VoidStatement extends Statement {
  private WasmExpression resolvedExpression;

  public VoidStatement(ExpressionNode expression) {
    super(expression);
  }

  @Override
  protected void resolveImpl(ValidationContext resolutionContext, int line) {
    WasmExpressionBuilder builder = new WasmExpressionBuilder();
    Type returnType = children[0].resolveWasm(builder, resolutionContext, line);
    if (returnType instanceof FunctionType) {
      // returnType = ((FunctionType) returnType).getReturnType();
      throw new RuntimeException("This special case shouldn't exist any longer.");
    }
    if (returnType != Types.VOID) {
      throw new ExceptionWithReplacementPropolsal(
          "Function result typed "+ returnType + " ignored.",
          "print %s");
    }
    resolvedExpression = builder.build();
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    resolvedExpression.run(evaluationContext).popObject();
    /*    if (result instanceof Callable) {
      evaluationContext.call((Callable) result, 0);
    } */
    return null;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    int start = 0;
    children[0].toString(asb, errors, preferAscii);
    asb.annotate(start, asb.length(), errors.get(this));
  }
}
