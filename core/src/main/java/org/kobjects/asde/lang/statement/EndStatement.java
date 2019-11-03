package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.type.CodeLine;

import java.util.Map;

public class EndStatement extends Statement {

  /**
   * An invisible END is inserted for single-line on statements at the end of the line. An alternative might be
   * some form of special single-line mode.
   */
  public final boolean invisible;

  public EndStatement(boolean invisible) {
    this.invisible = invisible;
  }

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
    if (!invisible && !resolutionContext.program.legacyMode) {
      CodeLine codeLine = resolutionContext.functionImplementation.findNextLine(line);
      if (codeLine.length() > 1) {
        throw new RuntimeException("END must be on a separate line.");
      }
    }
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    evaluationContext.currentLine = Integer.MAX_VALUE;
    return null;
  }


  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    if (!invisible) {
      appendLinked(asb, "END", errors);
    }
  }
}
