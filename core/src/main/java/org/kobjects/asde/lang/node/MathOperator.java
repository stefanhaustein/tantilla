package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.Types;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class MathOperator extends Node {

  public enum Kind {
    ADD, SUB, MUL, DIV, MOD, POW;
  }

  public final Kind kind;

  public boolean stringAdd;

  public MathOperator(Kind kind, Node child1, Node child2) {
    super(child1, child2);
    this.kind = kind;
  }

  String getName(boolean preferAscii) {
    switch (kind) {
      case ADD:
        return "+";
      case SUB:
        return preferAscii ? "-" : "−";
      case MUL:
        return preferAscii ? "*" : "×";
      case DIV:
        return "/";
      case POW:
        return "^";
      case MOD:
        return "%";
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, int line) {
    stringAdd = false;
    Type t0 = children[0].returnType();
    if (t0 != Types.BOOL && t0 != Types.FLOAT) {
      if (kind == Kind.ADD) {
        if (t0 == Types.STR) {
          stringAdd = true;
          return;
        }
        throw new RuntimeException("Left parameter type should be String or Number; got: " + t0);
      }
      throw new RuntimeException("Left parameter expected to be a Number but is " + t0);
    }
    Type t1 = children[0].returnType();
    if (t1 != Types.BOOL && t0 != Types.FLOAT) {
      throw new RuntimeException("Right parameter expected to be a Number but is " + t1);
    }
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    if (stringAdd) {
      return evalString(evaluationContext);
    }
    return evalDouble(evaluationContext);
  }

  @Override
  public String evalString(EvaluationContext evaluationContext) {
    return children[0].evalString(evaluationContext) + children[1].evalString(evaluationContext);
  }

  @Override
  public double evalDouble(EvaluationContext evaluationContext) {
    double l = children[0].evalDouble(evaluationContext);
    double r = children[1].evalDouble(evaluationContext);
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
      case MOD:
        return l % r;
      default:
        throw new IllegalStateException("Unsupported binary operator " + kind);
    }
  }

  @Override
  public Type returnType() {
    return stringAdd ? Types.STR : Types.FLOAT;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
      children[0].toString(asb, errors, preferAscii);
      if (kind == Kind.ADD || kind == Kind.SUB) {
        appendLinked(asb, ' ' + getName(preferAscii) + ' ', errors);
      } else {
        appendLinked(asb, getName(preferAscii), errors);
      }
      children[1].toString(asb, errors, preferAscii);

  }
}
