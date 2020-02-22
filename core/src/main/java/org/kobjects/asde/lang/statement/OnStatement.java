package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.PropertyValidationContext;
import org.kobjects.asde.lang.program.ProgramControl;
import org.kobjects.asde.lang.node.Node;


import java.util.ArrayList;
import java.util.Map;

public class OnStatement extends BlockStatement  {

  int resolvedEndLine;
  ArrayList<Node> listenableSubexpressions = new ArrayList<>();

  public OnStatement(Node condition) {
    super(condition);
  }


  @Override
  protected void onResolve(PropertyValidationContext resolutionContext, int line) {
    resolutionContext.startBlock(this);
    listenableSubexpressions.clear();
    findListenableSubexpressions(children);
  }

  void findListenableSubexpressions(Node[] nodes) {
    for (Node node: nodes) {
      if (node.returnType().supportsChangeListeners()) {
        listenableSubexpressions.add(node);
      } else {
        findListenableSubexpressions(node.children);
      }
    }
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    EvaluationContext newContectBase = new EvaluationContext(evaluationContext);
    newContectBase.currentLine++;

    Trigger trigger = new Trigger(newContectBase);
    for (Node node : listenableSubexpressions) {
      node.returnType().addChangeListener(node.eval(evaluationContext), trigger);
    }

    evaluationContext.currentLine = resolvedEndLine + 1;
    return null;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, "on ", errors);
    children[0].toString(asb, errors, preferAscii);
    asb.append(": ");
  }

  @Override
  public void onResolveEnd(PropertyValidationContext resolutionContext, EndStatement endStatement, int endLine) {
    resolvedEndLine = endLine;
  }

  @Override
  void evalEnd(EvaluationContext context) {
    context.currentLine = Integer.MAX_VALUE;
  }

  class Trigger implements  Runnable {

    final EvaluationContext evaluationContext;
    boolean armed = true;

    public Trigger(EvaluationContext evaluationContext) {
      this.evaluationContext = evaluationContext;
    }


    @Override
    public void run() {
      if (evaluationContext.control.getState() == ProgramControl.State.ABORTED ||
          evaluationContext.control.getState() == ProgramControl.State.ENDED) {
        return;
      }
      if (children[0].evalBoolean(evaluationContext)) {
//        System.out.println("Condition did trigger: " + OnStatement.this);
        if (armed) {
          armed = false;
          new Thread(() -> {
            try {
              evaluationContext.function.callImpl(new EvaluationContext(evaluationContext));
            } catch (Exception e) {
              e.printStackTrace();
            }
          }).start();
        }
      } else {
        armed = true;
      }
    }
  }

}
