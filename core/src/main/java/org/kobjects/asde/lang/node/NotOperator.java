package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Types;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class NotOperator extends Node {

  public NotOperator(Node child) {
    super(child);
  }

  public Object eval(Interpreter interpreter) {
    Object lVal = children[0].eval(interpreter);
    if (lVal instanceof Boolean) {
      return !((Boolean) lVal);
    }
    if (lVal instanceof Double) {
      return ~((Double) lVal).intValue();
    }
    throw new EvaluationException(children[0], "Boolean or Number expected for NOT.");
  }

  @Override
  public Type returnType() {
    return children[0].returnType() == Types.BOOLEAN ? Types.BOOLEAN : Types.NUMBER;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
    appendLinked(asb,"NOT ", errors);
    children[0].toString(asb, errors);
  }
}
