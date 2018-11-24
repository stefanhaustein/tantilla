package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Types;
import org.kobjects.asde.lang.parser.ResolutionContext;
import org.kobjects.typesystem.Type;

import java.util.Collections;
import java.util.Map;

public abstract class Node {

  public static final Node[] EMPTY_ARRAY = new Node[0];

  public Node[] children;

  protected Node(Node... children) {
    this.children = children == null || children.length == 0 ? EMPTY_ARRAY : children;
  }

  public void resolve(ResolutionContext resolutionContext) {
    for (Node child: children) {
      try {
        child.resolve(resolutionContext);
      } catch (Exception e) {
        resolutionContext.addError(child, e);
      }
    }
  }

  public abstract Object eval(Interpreter interpreter);

  /** eval without null substitution */
  public Object evalRaw(Interpreter interpreter) {
    return eval(interpreter);
  }

  public boolean evalChildToBoolean(Interpreter interpreter, int i) {
    Object o = children[i].eval(interpreter);
    if (o instanceof Boolean) {
      return (Boolean) o;
    }
    if (o instanceof Double) {
      return ((Double) o).doubleValue() != 0;
    }
    throw new EvaluationException(children[i], "Boolean or Number expected; got " + Types.of(o));
  }


  public double evalChildToDouble(Interpreter interpreter, int i) {
    Object o = children[i].eval(interpreter);
    if (o instanceof Double) {
      return (Double) o;
    }
    if (o instanceof Boolean) {
      return ((Boolean) o) ? 1.0 : 0.0;
    }
    throw new EvaluationException(children[i], "Number expected.");
  }

  public int evalChildToInt(Interpreter interpreter, int i) {
    return (int) evalChildToDouble(interpreter, i);
  }

  public String evalChildToString(Interpreter interpreter, int i) {
    return Program.toString(children[i].eval(interpreter));
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
}
