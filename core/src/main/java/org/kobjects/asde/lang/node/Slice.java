package org.kobjects.asde.lang.node;

import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.list.ListImpl;
import org.kobjects.asde.lang.list.ListType;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;


public class Slice extends Node {
  Type resolvedElementType;
  boolean string;

  public Slice(Node... children) {
    super(children);
  }

  @Override
  protected void onResolve(ValidationContext resolutionContext, int line) {
    Type type0 = children[0].returnType();
    string = type0 == Types.STR;
    if (!string && !(type0 instanceof ListType)) {
      throw new RuntimeException("Str or List base type expected.");
    }
    resolvedElementType = ((ListType) children[0].returnType()).elementType;
    for (int i = 1; i < children.length; i++) {
      if (children[i].returnType() != Types.FLOAT) {
        throw new RuntimeException("Slice arguments must be numbers.");
      }
    }
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    int start = (children[1] instanceof ImpliedSliceValue) ? 0 : children[1].evalInt(evaluationContext);
    if (string) {
      String s = children[0].evalString(evaluationContext);
      if (start < 0) {
        start = s.codePointCount(0, s.length()) - start;
      }
      int end = (children[2] instanceof ImpliedSliceValue) ? s.codePointCount(0, s.length()) : children[2].evalInt(evaluationContext);
      if (end < 0) {
        end = s.codePointCount(0, s.length()) - end;
      }
      int pos = 0;
      for (int i = 0; i < start; i++) {
        pos += Character.charCount(s.codePointAt(pos));
      }
      return s.substring(start, end);
    }
    ListImpl list = (ListImpl) children[0].eval(evaluationContext);
    if (start < 0) {
      start = list.length() - start;
    }
    int end = (children[2] instanceof ImpliedSliceValue) ? list.length() : children[2].evalInt(evaluationContext);
    if (end < 0) {
      end = list.length() - end;
    }
    Object[] data = new Object[end - start];
    for (int i = start; i < end; i++) {
      data[i - start] = list.get(i);
    }
    return new ListImpl(resolvedElementType, data);
  }

  @Override
  public Type returnType() {
    return children[0].returnType();
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    int start = asb.length();
    children[0].toString(asb, errors, preferAscii);
    asb.append('[');
    for (int i = 1; i < children.length; i++) {
      if (i > 1) {
        asb.append(":");
      }
      children[i].toString(asb, errors, preferAscii);
    }
    asb.append(']');
    asb.annotate(start, asb.length(), errors.get(this));
  }
}
