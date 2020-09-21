package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.wasm.Wasm;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;
import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Consumer;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Collections;
import java.util.Map;


public abstract class Node {

  // Used in error collections for property level errors.
  public static Node NO_NODE = new Node() {
    @Override
    protected void onResolve(ValidationContext resolutionContext, int line) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Object eval(EvaluationContext evaluationContext) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Type returnType() {
      throw new UnsupportedOperationException();
    }
  };

  public static final ExpressionNode[] EMPTY_ARRAY = new ExpressionNode[0];

  public ExpressionNode[] children;

  protected Node(ExpressionNode... children) {
    this.children = children == null || children.length == 0 ? EMPTY_ARRAY : children;
  }

  protected abstract void onResolve(ValidationContext resolutionContext, int line);



  public boolean resolve(ValidationContext resolutionContext, int line) {
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


  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    if (children != null && children.length > 0) {
      children[0].toString(asb, errors, preferAscii);
      for (int i = 1; i < children.length; i++) {
        asb.append(", ");
        children[i].toString(asb, errors, preferAscii);
      }
    }
  }

  public void rename(Property symbol) {
  }
  
  public void reorderParameters(Property symbol, int[] oldIndices) {
  }

  public void renameParameters(Map<String, String> renameMap) {
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

  @Override
  public final String toString() {
    AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
    toString(asb, Collections.<Node, Exception>emptyMap(), false);
    return asb.toString();
  }

  public abstract Type returnType();


  public void process(Consumer<Node> action) {
    for (Node child : children) {
      child.process(action);
    }
    action.accept(this);
  }
}
