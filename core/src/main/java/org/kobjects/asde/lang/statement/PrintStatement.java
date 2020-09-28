package org.kobjects.asde.lang.statement;

import org.kobjects.asde.lang.node.ExpressionNode;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;
import org.kobjects.asde.lang.wasm.runtime.WasmExpression;
import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.function.ValidationContext;

import java.util.Map;

public class PrintStatement extends Statement {

  WasmExpression[] resolvedExpressions;

  public PrintStatement(ExpressionNode... children) {
    super(children);
    resolvedExpressions = new WasmExpression[children.length];
  }

  @Override
  protected void resolveImpl(ValidationContext resolutionContext, int line) {
    for (int i = 0; i < children.length; i++) {
      WasmExpressionBuilder builder = new WasmExpressionBuilder();
      children[i].resolveWasm(builder, resolutionContext, line);
      resolvedExpressions[i] = builder.build();
    }
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    Program program = evaluationContext.control.program;
    for (WasmExpression wasmExpression : resolvedExpressions) {
      Object val = wasmExpression.run(evaluationContext).popObject();
      program.print(Program.toString(val));
    }
    return null;
  }


  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, "print", errors);
    if (children.length > 0) {
      appendLinked(asb, " ", errors);
      children[0].toString(asb, errors, preferAscii);
      for (int i = 1; i < children.length; i++) {
        asb.append(", ");
        children[i].toString(asb, errors, preferAscii);
      }
    }
  }
}
