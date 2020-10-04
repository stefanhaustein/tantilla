package org.kobjects.asde.lang.statement;

import org.kobjects.asde.lang.node.ExpressionNode;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;
import org.kobjects.asde.lang.wasm.runtime.WasmExpression;
import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.runtime.EvaluationContext;

import java.util.Map;

public class OnChangeStatement extends BlockStatement  {

  int resolvedEndLine;
  Type resolvedType;
  WasmExpression resolvedExpression;

  public OnChangeStatement(ExpressionNode listen) {
    super(listen);
  }


  @Override
  protected void resolveImpl(ValidationContext resolutionContext, int line) {
    resolutionContext.startBlock(this);

    WasmExpressionBuilder builder = new WasmExpressionBuilder();
    resolvedType = children[0].resolveWasm(builder, resolutionContext, line);

    if (!resolvedType.supportsChangeListeners()) {
      throw new RuntimeException("Expression does not support change notifications.");
    }

    resolvedExpression = builder.build();
  }


  @Override
  public Object eval(EvaluationContext evaluationContext) {
    EvaluationContext newContectBase = new EvaluationContext(evaluationContext);
    newContectBase.currentLine++;

    Trigger trigger = new Trigger(newContectBase);
    resolvedType.addChangeListener(resolvedExpression.run(evaluationContext).popObject(), trigger);

    evaluationContext.currentLine = resolvedEndLine + 1;
    return null;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, "onchange ", errors);
    children[0].toString(asb, errors, preferAscii);
    asb.append(": ");
  }

  @Override
  public void onResolveEnd(ValidationContext resolutionContext, EndStatement endStatement, int endLine) {
    resolvedEndLine = endLine;
  }

  @Override
  void evalEnd(EvaluationContext context) {
    context.currentLine = Integer.MAX_VALUE;
  }

  class Trigger implements Runnable {

    final EvaluationContext evaluationContext;

    public Trigger(EvaluationContext evaluationContext) {
      this.evaluationContext = evaluationContext;
    }

    @Override
    public void run() {

      new Thread(() -> {
            try {
              evaluationContext.function.callImpl(new EvaluationContext(evaluationContext));
            } catch (Exception e) {
              e.printStackTrace();
            }
          }).start();

      }
    }


}
