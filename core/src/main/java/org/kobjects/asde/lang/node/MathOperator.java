package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class MathOperator extends Node {

  public enum Kind {
    ADD, SUB, MUL, DIV, POW
  }

  public final Kind kind;

  public boolean stringAdd;

  public MathOperator(Kind kind, Node child1, Node child2) {
    super(child1, child2);
    this.kind = kind;
  }

  String getName() {
    switch (kind) {
      case ADD:
        return "+";
      case SUB:
        return "−";
      case MUL:
        return "×";
      case DIV:
        return "/";
      case POW:
        return "^";
        default:
          throw new IllegalStateException();
    }
  }

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
    stringAdd = false;
    Type t0 = children[0].returnType();
    if (t0 != Types.BOOLEAN && t0 != Types.NUMBER) {
      if (kind == Kind.ADD) {
        if (t0 == Types.STRING) {
          stringAdd = true;
          return;
        }
        throw new RuntimeException("Left parameter type should be String or Number; got: " + t0);
      }
      throw new RuntimeException("Left parameter expected to be a Number but is " + t0);
    }
    Type t1 = children[0].returnType();
    if (t1 != Types.BOOLEAN && t0 != Types.NUMBER) {
      throw new RuntimeException("Right parameter expected to be a Number but is " + t1);
    }
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    if (stringAdd) {
      return evalChildToString(evaluationContext, 0) + evalChildToString(evaluationContext, 1);
    }
    double l = evalChildToDouble(evaluationContext, 0);
    double r = evalChildToDouble(evaluationContext, 1);
    switch (kind) {
      case POW:
        return Math.pow(l, r);
      case ADD:
        return l + r;
      case SUB:
        return l - r;
      case DIV:
        return l / r;
      case MUL:
        return l * r;
      default:
        throw new IllegalStateException("Unsupported binary operator " + kind);
    }
  }

  @Override
  public Type returnType() {
    return stringAdd ? Types.STRING : Types.NUMBER;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
      children[0].toString(asb, errors);
      if (kind == Kind.ADD || kind == Kind.SUB) {
        appendLinked(asb, ' ' + getName() + ' ', errors);
      } else {
        appendLinked(asb, getName(), errors);
      }
      children[1].toString(asb, errors);

  }
}
