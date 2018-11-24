package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Types;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class AndOperator extends Node {

  public AndOperator(Node child1, Node child2) {
    super(child1, child2);
  }

  public Object eval(Interpreter interpreter) {
    Object lVal = children[0].eval(interpreter);
    if (lVal instanceof Boolean) {
      return ((Boolean) lVal) ? children[1].eval(interpreter) : Boolean.FALSE;
    }
    if (lVal instanceof Double) {
      return ((Double) lVal).intValue() & evalChildToInt(interpreter,1);
    }
    throw new EvaluationException(children[0], "Boolean or Number expected for AND.");
  }

  @Override
  public Type returnType() {
    return children[0].returnType() == Types.BOOLEAN ? children[1].returnType() : Types.NUMBER;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
    children[0].toString(asb, errors);
    appendLinked(asb, " AND ", errors);
    children[1].toString(asb, errors);
  }
}
