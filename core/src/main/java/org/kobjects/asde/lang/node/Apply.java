package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.symbol.StaticSymbol;
import org.kobjects.asde.lang.list.ListImpl;
import org.kobjects.asde.lang.list.ListType;
import org.kobjects.asde.lang.function.Function;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.Types;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Type;

import java.util.Map;

// Not static for access to the variables.
public class Apply extends Node {

  boolean parenthesis;

  public Apply(boolean parentesis, Node... children) {
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
  protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line) {
    if (!(children[0].returnType() instanceof FunctionType)) {
      throw new RuntimeException("Can't apply parameters to " + children[0].returnType());
    }
    FunctionType resolved = (FunctionType) children[0].returnType();
    // TODO: b/c optional params, add minParameterCount
    if (children.length - 1 > resolved.getParameterCount() || children.length - 1 < resolved.getMinParameterCount()) {
      throw new RuntimeException("Expected parameter count is "
          + resolved.getMinParameterCount() + ".."
          + resolved.getParameterCount() + " but got " + (children.length - 1) + " for " + this);
    }
    for (int i = 0; i < children.length - 1; i++) {
      if (!resolved.getParameterType(i).isAssignableFrom(children[i+1].returnType())) {
        throw new RuntimeException("Type mismatch for parameter " + i + ": expected: "
            + resolved.getParameterType(i) + " actual: " + children[i+1].returnType() + " base type: " + resolved);
      }
    }
  }

  public Object eval(EvaluationContext evaluationContext) {
        Object base = children[0].eval(evaluationContext);
        if (!(base instanceof Function)) {
          throw new EvaluationException(this, "Can't apply parameters to " + base + " / " + children[0]);
        }
        Function function = (Function) base;
        evaluationContext.ensureExtraStackSpace(function.getLocalVariableCount());
        if (children.length - 1 > function.getLocalVariableCount()) {
          throw new RuntimeException("Too many params for " + function);
        }
        // Push is important here, as parameter evaluation might also run apply().
        for (int i = 1; i < children.length; i++) {
          evaluationContext.push(children[i].eval(evaluationContext));
        }
        evaluationContext.popN(children.length - 1);
        try {
          return function.call(evaluationContext, children.length - 1);
        } catch (Exception e) {
          throw new RuntimeException(e.getMessage() + " in " + children[0], e);
        }
  }

  // Shouldn't throw, as it's used outside validation!
  public Type returnType() {
    return ((FunctionType) children[0].returnType()).getReturnType();
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    int start = asb.length();
    children[0].toString(asb, errors, preferAscii);
    asb.append(parenthesis ?  '(' : ' ');
    for (int i = 1; i < children.length; i++) {
      if (i > 1) {
        asb.append(", ");
      }
      children[i].toString(asb, errors, preferAscii);
    }
    if (parenthesis) {
      asb.append(')');
    }
    asb.annotate(start, asb.length(), errors.get(this));
  }
}
