package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Types;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class NegOperator extends Node {

  public NegOperator(Node child) {
    super(child);
  }

  public Object eval(Interpreter interpreter) {
    return -evalChildToDouble(interpreter, 0);
  }

  @Override
  public Type returnType() {
    return Types.NUMBER;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
    appendLinked(asb,"-", errors);
    children[0].toString(asb, errors);
  }
}
