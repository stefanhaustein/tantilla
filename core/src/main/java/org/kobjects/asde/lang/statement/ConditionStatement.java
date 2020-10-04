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

public class ConditionStatement extends BlockStatement {

  public final Kind kind;

  public enum Kind {
    IF, ELIF, ELSE
  }

  int resolvedEndLine;
  int resolvedLine;
  ConditionStatement resolvedPrevious;
  ConditionStatement resolvedNext;
  WasmExpression resolvedCondition;

  public ConditionStatement(Kind kind, ExpressionNode condition) {
    super(condition);
    this.kind = kind;
  }

  @Override
  public boolean closesBlock() {
    return kind != Kind.IF;
  }



  @Override
  public void resolveImpl(ValidationContext resolutionContext, int line) {
    resolvedLine = line;
    resolvedPrevious = null;
    resolvedNext = null;
    resolvedEndLine = Integer.MAX_VALUE;

    if (kind != Kind.IF) {
      Statement startStatement = resolutionContext.endBlock();
      if (!(startStatement instanceof ConditionStatement)) {
        throw new RuntimeException("The block start must be 'if' or 'elif', but was: " + resolutionContext.getCurrentBlock().startStatement);
      }
      resolvedPrevious = (ConditionStatement) startStatement;
      if (resolvedPrevious.kind == Kind.ELSE) {
        throw new RuntimeException("The block start must be 'if' or 'elif' for '" + kind.name().toLowerCase() + "' + but was 'else'.");
      }
      resolvedPrevious.resolvedNext = this;
    }

    WasmExpressionBuilder builder = new WasmExpressionBuilder();
    children[0].resolveWasm(builder, resolutionContext, line, Types.BOOL);
    resolvedCondition = builder.build();
    resolutionContext.startBlock(this);
  }


  @Override
  public Object eval(EvaluationContext evaluationContext) {
    if (kind == Kind.IF) {
      ConditionStatement current = this;
      while (true) {
        current.resolvedCondition.run(evaluationContext);
        if (evaluationContext.dataStack.popBoolean()) {
          break;
        }
        current = current.resolvedNext;
        if (current == null) {
          evaluationContext.currentLine = resolvedEndLine + 1;
          return null;
        }
      }
      evaluationContext.currentLine = current.resolvedLine + 1;
    } else {
      evaluationContext.currentLine = resolvedEndLine + 1;
    }
    return null;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, kind.name().toLowerCase(), errors, SyntaxColor.KEYWORD);
    if (kind != Kind.ELSE) {
      asb.append(' ');
      children[0].toString(asb, errors, preferAscii);
    }
    asb.append(":");
  }

  @Override
  public void onResolveEnd(ValidationContext resolutionContext, EndStatement endStatement, int endLine) {
    ConditionStatement current = this;
    do {
      current.resolvedEndLine = endLine;
      current = current.resolvedPrevious;
    } while (current != null);
  }

  @Override
  void evalEnd(EvaluationContext context) {
    // Reached by normal execution of the last else/elif block
  }
}
