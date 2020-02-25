package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.function.ValidationContext;

import java.util.Map;

public class PrintStatement extends Statement {

  public PrintStatement(Node... children) {
    super(children);
  }

  @Override
  protected void onResolve(ValidationContext resolutionContext, int line) {
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    Program program = evaluationContext.control.program;
    for (int i = 0; i < children.length; i++) {
      Object val = children[i].eval(evaluationContext);
      program.print(Program.toString(val));
    }
    return null;
  }


  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, "print", errors);
    if (children.length > 0) {
      appendLinked(asb, " ", errors);
      children[0].toString(asb, errors, preferAscii);
      for (int i = 1; i < children.length; i++) {
        asb.append(", ");
        children[i].toString(asb, errors, preferAscii);
      }
    }
  }
}
