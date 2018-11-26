package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Types;
import org.kobjects.asde.lang.parser.ResolutionContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class AndOperator extends Node {

  public AndOperator(Node child1, Node child2) {
    super(child1, child2);
  }

  public Object eval(Interpreter interpreter) {
    Object lVal = children[0].eval(interpreter);
    if (lVal instanceof Boolean) {
      return ((Boolean) lVal) ? evalChildToBoolean(interpreter, 1) : Boolean.FALSE;
    }
    if (lVal instanceof Double) {
      return ((Double) lVal).intValue() & evalChildToInt(interpreter,1);
    }
    throw new EvaluationException(children[0], "Boolean or Number expected for AND.");
  }

  @Override
  protected void onResolve(ResolutionContext resolutionContext) {
    if (!Types.match(children[0].returnType(), children[1].returnType())) {
      throw new RuntimeException("Matching Boolean or Number argument expected; got "
              + children[0].returnType() + " and " + children[1].returnType());
    }
    if (!Types.match(children[0].returnType(), Types.BOOLEAN)
            && !Types.match(children[0].returnType(), Types.NUMBER)) {
      throw new RuntimeException("Boolean or Number arguments expected; got: " + children[0].returnType());
    }
  }

  @Override
  public Type returnType() {
    return children[0].returnType() == Types.BOOLEAN || children[0].returnType() == Types.BOOLEAN
            ? children[0].returnType() : null;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
    children[0].toString(asb, errors);
    appendLinked(asb, " AND ", errors);
    children[1].toString(asb, errors);
  }
}
