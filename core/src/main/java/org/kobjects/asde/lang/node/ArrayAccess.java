package org.kobjects.asde.lang.node;

import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.list.ListImpl;
import org.kobjects.asde.lang.list.ListType;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.expressionparser.Tokenizer;
import org.kobjects.asde.lang.type.MetaType;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;

/**
 * Note that slicing is handled separately, as it can be identified at compile time.
 */
public class ArrayAccess extends AssignableNode {

  enum Kind {
    UNRESOLVED, ARRAY_ACCESS, QUALIFIED_TYPE, LIST_CONSTRUCTOR, STRING_ACCESS, ERROR
  }

  Kind kind = Kind.UNRESOLVED;
  Type resolvedElementType;
  // Only used for LIST_CONSTRUCTOR
  Node[] resolvedChildren;

  public ArrayAccess(Node... children) {
    super(children);
  }

  @Override
  public Type resolveForAssignment(ValidationContext resolutionContext, int line) {
    resolve(resolutionContext, line);
    if (kind != Kind.ARRAY_ACCESS && kind != Kind.ERROR) {
      throw new RuntimeException("Array expected");
    }
    return returnType();
  }

  public void set(EvaluationContext evaluationContext, Object value) {
    ListImpl list = (ListImpl) children[0].eval(evaluationContext);
    list.setValueAt(value, children[1].evalInt(evaluationContext));
  }

  @Override
  protected void onResolve(ValidationContext resolutionContext, int line) {
    kind = Kind.ERROR;
    Type type0 = children[0].returnType();
    if (type0 instanceof ListType || type0 == Types.STR) {
      kind = type0 == Types.STR ? Kind.STRING_ACCESS : Kind.ARRAY_ACCESS;
      if (children.length != 2) {
        throw new RuntimeException("Exactly one array index argument expected");
      }
      if (children[1].returnType() != Types.FLOAT) {
        throw new RuntimeException("Number argument expected for array access; got: " + children[1].returnType());
      }
    } else if (children[0].toString().equals("List")) {
      // The reason for parsing the type is the ambiguity of float (conversion method vs. type).
      kind = Kind.QUALIFIED_TYPE;
      Tokenizer tokenizer = resolutionContext.program.parser.createTokenizer(toString());
      tokenizer.nextToken();
      resolvedElementType = resolutionContext.program.parser.parseType(tokenizer);
    } else if (children[0].returnType() instanceof MetaType) {
      kind = Kind.LIST_CONSTRUCTOR;
      Type inner = ((MetaType) children[0].returnType()).getWrapped();
      if (!(inner instanceof ListType)) {
        throw new RuntimeException("List type expected for list constructor");
      }
      resolvedElementType = ((ListType) inner).elementType;
      resolvedChildren = new Node[children.length - 1];
      for (int i = 0; i < resolvedChildren.length; i++) {
        resolvedChildren[i] = TraitCast.autoCast(children[i+1], resolvedElementType, resolutionContext);
      }
    } else {
      throw new RuntimeException("Not a list: " + children[0] + " (" + children[0].returnType() + ") -- this: " + this);
    }
  }

  public Object eval(EvaluationContext evaluationContext) {
    switch (kind) {
      case ARRAY_ACCESS: {
        ListImpl array = (ListImpl) children[0].eval(evaluationContext);
        int index = children[1].evalInt(evaluationContext);
        return array.get(index < 0 ? array.length() - index : index);
      }
      case STRING_ACCESS: {
        String s = (String) children[0].eval(evaluationContext);
        int index = children[1].evalInt(evaluationContext);
        if (index < 0) {
          index = s.codePointCount(0, s.length()) - index;
        }
        int pos = 0;
        for (int i = 0; i < index; i++) {
          pos += Character.charCount(s.codePointAt(pos));
        }
        return s.substring(pos, pos + Character.charCount(pos));
      }
      case QUALIFIED_TYPE:
        return resolvedElementType;
      case LIST_CONSTRUCTOR:
        Object[] data = new Object[resolvedChildren.length];
        for (int i = 0; i < data.length; i++) {
          data[i] = resolvedChildren[i].eval(evaluationContext);
        }
        return new ListImpl(resolvedElementType, data);
    }
    throw new IllegalStateException();
  }

  // Shouldn't throw, as it's used outside validation!
  public Type returnType() {
    switch (kind) {
      case STRING_ACCESS:
        return Types.STR;
      case ARRAY_ACCESS:
        return ((ListType) children[0].returnType()).elementType;
      case QUALIFIED_TYPE:
        return new MetaType(resolvedElementType);
      case LIST_CONSTRUCTOR:
        return new ListType(resolvedElementType);
      default:
        return null;
    }
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    int start = asb.length();
    children[0].toString(asb, errors, preferAscii);
    asb.append('[');
    for (int i = 1; i < children.length; i++) {
      if (i > 1) {
        asb.append(", ");
      }
      children[i].toString(asb, errors, preferAscii);
    }
    asb.append(']');
    asb.annotate(start, asb.length(), errors.get(this));
  }
}
