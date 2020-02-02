package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.function.Function;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.asde.lang.function.Types;
import org.kobjects.asde.lang.list.ListImpl;
import org.kobjects.asde.lang.list.ListType;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.symbol.StaticSymbol;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Type;

import java.util.Map;

// Not static for access to the variables.
public class ArrayAccess extends AssignableNode {

  public ArrayAccess(Node... children) {
    super(children);
    if (children.length != 2) {
      throw new RuntimeException("Exactly two children expected.");
    }
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


  // Resolves "base" node last, which allows identifiers to access parameter types
  public void resolve(FunctionValidationContext resolutionContext, Node parent, int line) {
    for (int i = 1; i < children.length; i++) {
      children[i].resolve(resolutionContext, this, line);
    }
    children[0].resolve(resolutionContext, this, line);
    try {
      onResolve(resolutionContext, parent, line);
    } catch (Exception e) {
      resolutionContext.addError(this, e);
    }
  }


  @Override
  public void resolveForAssignment(FunctionValidationContext resolutionContext, Node parent, Type type, int line) {
    resolve(resolutionContext, parent, line);

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
  public boolean isAssignable() {
    return children[0].returnType() instanceof ListType;
  }

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line) {
    if (!(children[0].returnType() instanceof ListType)) {
      throw new RuntimeException("Not a list: " + children[0].returnType());
    }
      for (int i = 1; i < children.length; i++) {
        if (children[i].returnType() != Types.FLOAT) {
          throw new RuntimeException("Number expected for paramter " + i + "; got: " + children[i].returnType());
        }
      }
  }

  public Object eval(EvaluationContext evaluationContext) {
    ListImpl array = (ListImpl) children[0].eval(evaluationContext);
    return array.get(children[1].evalInt(evaluationContext));
  }

  // Shouldn't throw, as it's used outside validation!
  public Type returnType() {
    return ((ListType) children[0].returnType()).elementType;
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
