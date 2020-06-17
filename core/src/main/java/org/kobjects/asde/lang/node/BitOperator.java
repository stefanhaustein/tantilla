package org.kobjects.asde.lang.node;

import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.type.Types;

import java.util.Map;

public class BitOperator extends Node {
  public enum Kind {
    AND, OR, XOR, SHL, SHR
  }

  private final Kind kind;


  public BitOperator(BitOperator.Kind kind, Node child1, Node child2) {
    super(child1, child2);
    this.kind = kind;
  }

  String getName(boolean preferAscii) {
    switch (kind) {
      case AND:
        return "&";
      case OR:
        return "|";
      case XOR:
        return "^";
      case SHL:
        return "<<";
      case SHR:
        return ">>";
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  protected void onResolve(ValidationContext resolutionContext, int line) {
    Type t0 = children[0].returnType();
    if (t0 != Types.FLOAT) {
      throw new RuntimeException("Left parameter expected to be a Number but is " + t0);
    }
    Type t1 = children[0].returnType();
    if (t1 != Types.FLOAT) {
      throw new RuntimeException("Right parameter expected to be a Number but is " + t1);
    }
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    return (double) evalInt(evaluationContext);
  }

  @Override
  public int evalInt(EvaluationContext evaluationContext) {
    int l = children[0].evalInt(evaluationContext);
    int r = children[1].evalInt(evaluationContext);
    switch (kind) {
      case AND:
        return l & r;
      case OR:
        return l | r;
      case XOR:
        return l ^ r;
      case SHL:
        return l << r;
      case SHR:
        return l >> r;
      default:
        throw new IllegalStateException("Unsupported binary operator " + kind);
    }
  }

  @Override
  public Type returnType() {
    return Types.FLOAT;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    children[0].toString(asb, errors, preferAscii);
    appendLinked(asb, ' ' + getName(preferAscii) + ' ', errors);
    children[1].toString(asb, errors, preferAscii);
  }
}
