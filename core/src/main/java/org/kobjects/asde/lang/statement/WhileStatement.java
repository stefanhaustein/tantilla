package org.kobjects.asde.lang.statement;

import org.kobjects.asde.lang.expression.ExpressionNode;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;
import org.kobjects.asde.lang.wasm.runtime.WasmExpression;
import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.io.SyntaxColor;
import org.kobjects.asde.lang.expression.Node;
import org.kobjects.asde.lang.runtime.EvaluationContext;

import java.util.Map;

public class WhileStatement extends BlockStatement {
  int resolvedStartLine;
  int resolvedEndLine;
  WasmExpression resolvedCondition;

  public WhileStatement(ExpressionNode condition) {
    super(condition);
  }

  @Override
  public void onResolveEnd(ValidationContext resolutionContext, EndStatement endStatement, int endLine) {
    resolvedEndLine = endLine;
  }

  @Override
  void evalEnd(EvaluationContext context) {
    eval(context);
  }

  @Override
  protected void resolveImpl(ValidationContext resolutionContext, int line) {
    resolvedStartLine = line;
    WasmExpressionBuilder builder = new WasmExpressionBuilder();
    children[0].resolveWasm(builder, resolutionContext, line, Types.BOOL);
    resolvedCondition = builder.build();
    resolutionContext.startBlock(this);
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    if (resolvedCondition.run(evaluationContext).popBoolean()) {
      evaluationContext.currentLine = resolvedStartLine + 1;
    } else {
      evaluationContext.currentLine = resolvedEndLine + 1;
    }
    return null;
  }


  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, "while", errors, SyntaxColor.KEYWORD);
    asb.append(' ');
    children[0].toString(asb, errors, preferAscii);
    asb.append(":");
  }
}
