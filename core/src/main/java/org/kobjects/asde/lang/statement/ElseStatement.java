package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.StatementMatcher;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.asde.lang.function.CodeLine;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class ElseStatement extends Node {

  public final boolean multiline;

  public int resolvedLine;
  public int resolvedIndex;

  public ElseStatement(boolean multiline) {
    this.multiline = multiline;
  }

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
    resolutionContext.endBlock(FunctionValidationContext.BlockType.IF);
    resolutionContext.startBlock(FunctionValidationContext.BlockType.IF);
    if (multiline) {
      EndifMatcher matcher = new EndifMatcher();
      int[] pos = new int[] {line + 1, 0};
      resolutionContext.functionImplementation.find(matcher, pos);
      resolvedLine = pos[0];
      resolvedIndex = pos[1] + 1;
    } else {
      resolvedIndex = 0;
      resolvedLine = line + 1;
    }
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    evaluationContext.currentLine = resolvedLine;
    evaluationContext.currentIndex = resolvedIndex;
    return null;
  }

  @Override
  public Type returnType() {
    return null;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, "ELSE", errors);
  }


  /**
   * Else is reached only after a successful if and hence always goes to the end
   */
  static class EndifMatcher implements StatementMatcher {
    int level;

    @Override
    public boolean statementMatches(CodeLine line, int index, Node statement) {
      if (statement instanceof IfStatement && ((IfStatement) statement).multiline && !((IfStatement) statement).elseIf) {
        level++;
      } else if (statement instanceof EndIfStatement) {
        if (level == 0) {
          return true;
        }
        level--;
      }
      return false;
    }
  }
}