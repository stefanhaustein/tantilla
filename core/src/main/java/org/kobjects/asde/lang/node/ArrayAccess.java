package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.function.PropertyValidationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.list.ListImpl;
import org.kobjects.asde.lang.list.ListType;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.symbol.StaticSymbol;
import org.kobjects.expressionparser.Tokenizer;
import org.kobjects.asde.lang.type.MetaType;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;

// Not static for access to the variables.
public class ArrayAccess extends AssignableNode {

  enum Kind {
    ERROR, ARRAY_ACCES, QUALIFIED_TYPE, LIST_CONSTRUCTOR
  }

  Kind kind = Kind.ERROR;
  Type resolvedType;

  public ArrayAccess(Node... children) {
    super(children);
  }

  @Override
  public void changeSignature(StaticSymbol symbol, int[] newOrder) {
    Node base = children[0];
    if (!(base instanceof SymbolNode) || !((SymbolNode) base).matches(symbol, symbol.getName())) {
      return;
    }
    Node[] oldChildren = children;
    children = new Node[newOrder.length + 1];
    children[0] = base;
    for (int i = 0; i < newOrder.length; i++) {
      if (newOrder[i] != -1) {
        children[i + 1] = oldChildren[newOrder[i] + 1];
      } else {
        children[i + 1] = new Identifier("placeholder" + i);
      }
    }
  }

  @Override
  public void resolveForAssignment(PropertyValidationContext resolutionContext, Type type, int line) {
    resolve(resolutionContext, line);

    if (!(children[0].returnType() instanceof ListType)) {
      throw new RuntimeException("Array expected");
    }

    if (!type.isAssignableFrom(returnType())) {
      throw new RuntimeException("Expected type for assignment: " + type + " actual type: " + returnType());
    }
  }


  public void set(EvaluationContext evaluationContext, Object value) {
    Object base = children[0].eval(evaluationContext);
    ListImpl array = (ListImpl) base;
    int[] indices = new int[children.length - 1];
    for (int i = 1; i < children.length; i++) {
      indices[i - 1] = children[i].evalInt(evaluationContext);
    }
    array.setValueAt(value, indices);
  }

  @Override
  public boolean isConstant() {
    return false;
  }

  @Override
  protected void onResolve(PropertyValidationContext resolutionContext, int line) {
    kind = Kind.ERROR;
    if (children[0].returnType() instanceof ListType) {
      kind = Kind.ARRAY_ACCES;
      if (children.length != 2) {
        throw new RuntimeException("Exactly one array index argument expected");
      }
      if (children[1].returnType() != Types.FLOAT) {
        throw new RuntimeException("Number argument expected for array access; got: " + children[1].returnType());
      }
    } else if (children[0].toString().equals("List")) {
      // The reason for parsing the type is the ambiguity of float (conversion method vs. type).
      kind = Kind.QUALIFIED_TYPE;
      Tokenizer tokenizer = resolutionContext.program.parser.createTokenizer(toString());
      tokenizer.nextToken();
      resolvedType = resolutionContext.program.parser.parseType(tokenizer);
    } else if (children[0].returnType() instanceof MetaType) {
      kind = Kind.LIST_CONSTRUCTOR;
      Type inner = ((MetaType) children[0].returnType()).getWrapped();
      if (!(inner instanceof ListType)) {
        throw new RuntimeException("List type expected for list constructor");
      }
      resolvedType = ((ListType) inner).elementType;
    } else {
      throw new RuntimeException("Not a list: " + children[0] + " (" + children[0].returnType() + ") -- this: " + this);
    }
  }

  public Object eval(EvaluationContext evaluationContext) {
    switch (kind) {
      case ARRAY_ACCES:
        ListImpl array = (ListImpl) children[0].eval(evaluationContext);
        return array.get(children[1].evalInt(evaluationContext));
      case QUALIFIED_TYPE:
        return resolvedType;
      case LIST_CONSTRUCTOR:
        Object[] data = new Object[children.length - 1];
        for (int i = 0; i < data.length; i++) {
          data[i] = children[i+1].eval(evaluationContext);
        }
        return new ListImpl(resolvedType, data);
    }
    throw new IllegalStateException();
  }

  // Shouldn't throw, as it's used outside validation!
  public Type returnType() {
    switch (kind) {
      case ARRAY_ACCES:
        return ((ListType) children[0].returnType()).elementType;
      case QUALIFIED_TYPE:
        return new MetaType(resolvedType);
      case LIST_CONSTRUCTOR:
        return new ListType(resolvedType);
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
