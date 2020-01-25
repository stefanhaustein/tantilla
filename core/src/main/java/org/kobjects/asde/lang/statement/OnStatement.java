package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.asde.lang.program.ProgramControl;
import org.kobjects.asde.lang.function.StatementMatcher;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.node.NodeProcessor;
import org.kobjects.asde.lang.function.CodeLine;
import org.kobjects.typesystem.Property;
import org.kobjects.typesystem.PropertyChangeListener;

import java.util.Map;

public class OnStatement extends BlockStatement  {

  int resolvedEndLine;
  int resolvedEndIndex;

  public OnStatement(Node condition) {
    super(condition);
  }


  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
    resolutionContext.startBlock(this, line, index);
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    EvaluationContext newContectBase = new EvaluationContext(evaluationContext);
    newContectBase.currentIndex++;

    new NodeProcessor(node -> node.addPropertyChangeListener(evaluationContext, new Trigger(newContectBase)))
        .processNode(children[0]);

    evaluationContext.currentLine = resolvedEndLine;
    evaluationContext.currentIndex = resolvedEndIndex + 1;
    return null;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, "on ", errors);
    children[0].toString(asb, errors, preferAscii);
    asb.append(": ");
  }

  @Override
  public void onResolveEnd(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
    resolvedEndLine = line;
    resolvedEndIndex = index;
  }

  @Override
  void evalEnd(EvaluationContext context) {
    context.currentLine = Integer.MAX_VALUE;
  }

  class Trigger implements PropertyChangeListener {

    final EvaluationContext evaluationContext;

    public Trigger(EvaluationContext evaluationContext) {
      this.evaluationContext = evaluationContext;
    }


    @Override
    public void propertyChanged(Property<?> property) {
      if (evaluationContext.control.getState() == ProgramControl.State.ABORTED ||
          evaluationContext.control.getState() == ProgramControl.State.ENDED) {
        return;
      }
      if (children[0].evalBoolean(evaluationContext)) {
//        System.out.println("Condition did trigger: " + OnStatement.this);
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

}
