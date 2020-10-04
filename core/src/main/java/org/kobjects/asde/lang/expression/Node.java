package org.kobjects.asde.lang.expression;

import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Consumer;
import org.kobjects.asde.lang.classifier.Property;

import java.util.Collections;
import java.util.Map;


public abstract class Node {

  // Used in error collections for property level errors.
  public static Node NO_NODE = new Node() {};

  public static final ExpressionNode[] EMPTY_ARRAY = new ExpressionNode[0];

  public ExpressionNode[] children;

  protected Node(ExpressionNode... children) {
    this.children = children == null || children.length == 0 ? EMPTY_ARRAY : children;
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

  public void process(Consumer<Node> action) {
    for (Node child : children) {
      child.process(action);
    }
    action.accept(this);
  }
}
