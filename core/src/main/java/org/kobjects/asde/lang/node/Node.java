package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.type.CodeLine;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.typesystem.Type;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public abstract class Node {

  public static final Node[] EMPTY_ARRAY = new Node[0];

  public Node[] children;

  protected Node(Node... children) {
    this.children = children == null || children.length == 0 ? EMPTY_ARRAY : children;
  }

  public void accept(Visitor visitor) {
    visitor.visitNode(this);
  }

  protected abstract void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index);

  public void resolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
    for (Node child: children) {
      child.resolve(resolutionContext, this, line, index);
    }
    try {
      onResolve(resolutionContext, parent, line, index);
    } catch (Exception e) {
      resolutionContext.addError(this, e);
    }
  }

  public abstract Object eval(EvaluationContext evaluationContext);

  /** eval without null substitution */
  public Object evalRaw(EvaluationContext evaluationContext) {
    return eval(evaluationContext);
  }

  public boolean evalChildToBoolean(EvaluationContext evaluationContext, int i) {
    Object o = children[i].eval(evaluationContext);
    if (o instanceof Boolean) {
      return (Boolean) o;
    }
    if (o instanceof Double) {
      return ((Double) o).doubleValue() != 0;
    }
    throw new EvaluationException(children[i], "Boolean or Number expected; got " + Types.of(o));
  }


  public double evalChildToDouble(EvaluationContext evaluationContext, int i) {
    Object o = children[i].eval(evaluationContext);
    if (o instanceof Double) {
      return (Double) o;
    }
    if (o instanceof Boolean) {
      return ((Boolean) o) ? 1.0 : 0.0;
    }
    throw new EvaluationException(children[i], "Number expected.");
  }

  public int evalChildToInt(EvaluationContext evaluationContext, int i) {
    return (int) evalChildToDouble(evaluationContext, i);
  }

  public String evalChildToString(EvaluationContext evaluationContext, int i) {
    return Program.toString(children[i].eval(evaluationContext));
  }

  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
    if (children.length > 0) {
      children[0].toString(asb, errors);
      for (int i = 1; i < children.length; i++) {
        asb.append(", ");
        children[i].toString(asb, errors);
      }
    }
  }

  protected void appendLinked(AnnotatedStringBuilder asb, String s, Map<Node, Exception> errors) {
    asb.append(s, errors.get(this));
  }

  public final String toString() {
    AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
    toString(asb, Collections.<Node, Exception>emptyMap());
    return asb.toString();
  }

  public abstract Type returnType();


  public void renumber(TreeMap<Integer, CodeLine> renumbered) {
    for (Node child : children) {
      child.renumber(renumbered);
    }
  }
}
