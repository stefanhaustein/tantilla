package org.kobjects.asde.lang.node;

import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;

public class Named extends Node {

  public final String name;
  public Named(String name, Node value) {
    super(value);
    this.name = name;
  }

  @Override
  protected void onResolve(ValidationContext resolutionContext, int line) {
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    throw new IllegalStateException();
  }

  @Override
  public Type returnType() {
    return null;
  }


  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, name + " = ", errors);
    children[0].toString(asb, errors, preferAscii);
  }
}
