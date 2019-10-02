package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class UnparseableStatement extends Node {

  private final String text;
  private final RuntimeException error;

  public UnparseableStatement(String text, Exception error) {
    this.text = text;
    this.error = error instanceof RuntimeException ? (RuntimeException) error : new RuntimeException(error);
  }

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
    throw error;
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    return null;
  }

  @Override
  public Type returnType() {
    return Types.VOID;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
    appendLinked(asb, text, errors);
  }
}
