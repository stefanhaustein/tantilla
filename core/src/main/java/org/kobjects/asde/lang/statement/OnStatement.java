package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.asde.lang.ProgramControl;
import org.kobjects.asde.lang.StatementMatcher;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.node.Path;
import org.kobjects.asde.lang.node.Visitor;
import org.kobjects.asde.lang.type.CodeLine;
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
    CodeLine codeLine = resolutionContext.functionImplementation.ceilingEntry(line).getValue();
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
    children[0].accept(new ListenerAttachmentVisitor(newContectBase));
    evaluationContext.currentLine = lineBeyondEnd;
    return null;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
    appendLinked(asb, "ON ", errors);
    children[0].toString(asb, errors);
  }

  class ListenerAttachmentVisitor extends Visitor implements PropertyChangeListener {

    final EvaluationContext evaluationContext;

    public ListenerAttachmentVisitor(EvaluationContext evaluationContext) {
      this.evaluationContext = evaluationContext;
    }


    /*
    @Override
    public void visitIdentifier(Identifier identifier) {
      // identifier.eval(evaluationContext);

    }
    */


    @Override
    public void propertyChanged(Property<?> property) {
      if (evaluationContext.control.getState() == ProgramControl.State.ABORTED ||
          evaluationContext.control.getState() == ProgramControl.State.ENDED) {
        return;
      }
      if (evalChildToBoolean(evaluationContext, 0)) {
        System.out.println("Condition did trigger");
        new Thread(() -> {
          try {
            System.out.println("Thread started");
            evaluationContext.function.callImpl(new EvaluationContext(evaluationContext));
            System.out.println("Thread ended");
          } catch (Exception e) {
            e.printStackTrace();
          }
        }).start();
      } else {
        System.out.println("*** condition did not trigger ***");
      }
    }


    @Override
    public void visitPath(Path path) {
      Property property = path.evalProperty(evaluationContext);
      property.addListener(this);
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
