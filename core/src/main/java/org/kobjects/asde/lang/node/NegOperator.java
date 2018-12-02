package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Types;
import org.kobjects.asde.lang.parser.ResolutionContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class NegOperator extends Node {

  public NegOperator(Node child) {
    super(child);
  }

  @Override
  protected void onResolve(ResolutionContext resolutionContext, int line, int index) {
    if (!Types.match(children[0].returnType(), Types.NUMBER)) {
      throw new RuntimeException("Number argument expected for negation.");
    }
  }

  public Object eval(Interpreter interpreter) {
    return -evalChildToDouble(interpreter, 0);
  }

  @Override
  public Type returnType() {
    return children[0].returnType() == Types.NUMBER ? Types.NUMBER : null;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
    appendLinked(asb,"-", errors);
    children[0].toString(asb, errors);
  }
}
