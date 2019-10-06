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
    boolean bothNumber = Types.match(children[0].returnType(), Types.NUMBER)
            && Types.match(children[1].returnType(), Types.NUMBER);
    if (kind == Kind.ADD) {
      if (!Types.match(children[0].returnType(), Types.STRING) && !bothNumber) {
        throw new RuntimeException("Number or String arguments expected for '+'");
      }
    } else {
      if (!bothNumber) {
        throw new RuntimeException("Number arguments expected for " + getName());
      }
    }
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    double l;
    if (kind == Kind.ADD) {
      Object lVal = children[0].eval(evaluationContext);
      if (lVal instanceof Double) {
        l = ((Double) lVal).doubleValue();
      } else if (lVal instanceof Boolean) {
        l = ((Boolean) lVal) ? 1.0 : 0.0;
      } else if (lVal instanceof String) {
        return "" + lVal + evalChildToString(evaluationContext, 1);
      } else {
        throw new EvaluationException(children[0], "Number or String expected for operator '+'");
      }
    } else {
      l = evalChildToDouble(evaluationContext, 0);
    }
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
    return (kind == Kind.ADD && children[0].returnType() == Types.STRING) ? Types.STRING : Types.NUMBER;
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
