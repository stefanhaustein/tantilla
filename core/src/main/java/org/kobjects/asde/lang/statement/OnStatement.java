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

public class OnStatement extends Statement  {

  int lineBeyondEnd;

  public OnStatement(Node condition) {
    super(condition);
  }

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
    CodeLine codeLine = resolutionContext.functionImplementation.findNextLine(line);
    if (codeLine.length() > index + 1) {
      lineBeyondEnd = line + 1;
      if (!(codeLine.get(codeLine.length() - 1) instanceof EndStatement)) {
        codeLine.append(new EndStatement(true));
      }

    } else {
      int[] pos = {line + 1, 0};
      if (resolutionContext.functionImplementation.find(new EndMatcher(), pos) == null) {
        throw new RuntimeException("END not found for multiline ON.");
      }
      lineBeyondEnd = pos[0] + 1;
    }
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    EvaluationContext newContectBase = new EvaluationContext(evaluationContext);
    newContectBase.currentIndex++;

    new NodeProcessor(node -> node.addPropertyChangeListener(evaluationContext, new Trigger(newContectBase)))
        .processNode(children[0]);

    evaluationContext.currentLine = lineBeyondEnd;
    return null;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, "ON ", errors);
    children[0].toString(asb, errors, preferAscii);
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


  static class EndMatcher implements StatementMatcher {
    int skip;

    @Override
    public boolean statementMatches(CodeLine line, int index, Node statement) {
      if (statement instanceof OnStatement) {
        // Multiline?
        if (index == line.length() - 1) {
          skip++;
        }
      } else if (statement instanceof EndStatement) {
        if (skip == 0) {
          return true;
        }
        skip--;
      }
      return false;
    }
  }
}
