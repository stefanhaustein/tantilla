package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.program.ProgramControl;
import org.kobjects.asde.lang.property.Property;
import org.kobjects.asde.lang.property.PropertyChangeListener;
import org.kobjects.asde.lang.runtime.EvaluationContext;

import java.util.ArrayList;
import java.util.Map;

public class OnChangeStatement extends BlockStatement  {

  int resolvedEndLine;

  public OnChangeStatement(Node listen) {
    super(listen);
  }


  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, int line) {
    resolutionContext.startBlock(this);
    if (!children[0].returnType().supportsChangeListeners()) {
      throw new RuntimeException("Expression does not support change notifications.");
    }
  }


  @Override
  public Object eval(EvaluationContext evaluationContext) {
    EvaluationContext newContectBase = new EvaluationContext(evaluationContext);
    newContectBase.currentLine++;

    Trigger trigger = new Trigger(newContectBase);
    children[0].returnType().addChangeListener(children[0].eval(evaluationContext), trigger);

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
  public void onResolveEnd(FunctionValidationContext resolutionContext, EndStatement endStatement, int endLine) {
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
