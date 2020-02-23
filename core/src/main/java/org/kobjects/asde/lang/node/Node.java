package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.classifier.UserProperty;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.function.PropertyValidationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Collections;
import java.util.Map;


public abstract class Node {

  public static final Node[] EMPTY_ARRAY = new Node[0];

  public Node[] children;

  protected Node(Node... children) {
    this.children = children == null || children.length == 0 ? EMPTY_ARRAY : children;
  }

  protected abstract void onResolve(PropertyValidationContext resolutionContext, int line);

  public boolean resolve(PropertyValidationContext resolutionContext, int line) {
    for (Node child: children) {
      if (!child.resolve(resolutionContext, line)) {
        return false;
      }
    }
    try {
      onResolve(resolutionContext, line);
      return true;
    } catch (Exception e) {
      resolutionContext.addError(this, e);
      return false;
    }
  }

  public abstract Object eval(EvaluationContext evaluationContext);

  public boolean evalBoolean(EvaluationContext evaluationContext) {
    Object o = eval(evaluationContext);
    if (o instanceof Boolean) {
      return (Boolean) o;
    }
    if (o instanceof Number) {
      return ((Number) o).doubleValue() != 0;
    }
    throw new EvaluationException(this, "Boolean or Number expected; got " + Types.of(o));
  }

  public double evalDouble(EvaluationContext evaluationContext) {
    Object o = eval(evaluationContext);
    if (o instanceof Number) {
      return ((Number) o).doubleValue();
    }
    if (o instanceof Boolean) {
      return ((Boolean) o) ? 1.0 : 0.0;
    }
    throw new EvaluationException(this, "Number expected.");
  }

  public int evalInt(EvaluationContext evaluationContext) {
    Object o = eval(evaluationContext);
    if (o instanceof Number) {
      return ((Number) o).intValue();
    }
    if (o instanceof Boolean) {
      return ((Boolean) o) ? 1 : 0;
    }
    throw new EvaluationException(this, "Number expected.");
  }

  public String evalString(EvaluationContext evaluationContext) {
    return Program.toString(eval(evaluationContext));
  }

  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    if (children.length > 0) {
      children[0].toString(asb, errors, preferAscii);
      for (int i = 1; i < children.length; i++) {
        asb.append(", ");
        children[i].toString(asb, errors, preferAscii);
      }
    }
  }

  public void rename(UserProperty symbol, String oldName, String newName) {
  }
  
  public void changeSignature(UserProperty symbol, int[] newOrder) {
  }

  protected void appendLinked(AnnotatedStringBuilder asb, String s, Map<Node, Exception> errors, Object... annotations) {
    int p0 = asb.length();
    asb.append(s, errors.get(this));
    for (Object annotation : annotations) {
      if (annotation != null) {
        asb.annotate(p0, asb.length(), annotation);
      }
    }
  }

  public final String toString() {
    AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
    toString(asb, Collections.<Node, Exception>emptyMap(), false);
    return asb.toString();
  }

  public abstract Type returnType();

}
