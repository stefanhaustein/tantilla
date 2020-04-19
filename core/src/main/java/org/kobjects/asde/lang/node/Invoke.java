package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.classifier.InstantiableClassType;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.function.Callable;
import org.kobjects.asde.lang.io.SyntaxColor;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.type.MetaType;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;

// Not static for access to the variables.
public class Invoke extends Node {

  enum Kind {
    UNRESOLVED, CONSTRUCTOR, FUNCTION, ERROR
  }

  boolean parenthesis;
  Node[] resolvedArguments;
  Kind kind = Kind.UNRESOLVED;

  public Invoke(boolean parentesis, Node... children) {
    super(children);
    this.parenthesis = parentesis;
  }


  @Override
  public void reorderParameters(Property symbol, int[] oldIndices) {
    Node base = children[0];
    if (!(base instanceof SymbolNode) || ((SymbolNode) base).getResolvedProperty() != symbol) {
      return;
    }
    Node[] oldChildren = children;
    children = new Node[oldIndices.length + 1];
    children[0] = base;
    for (int i = 0; i < oldIndices.length; i++) {
      if (oldIndices[i] != -1) {
        children[i + 1] = oldChildren[oldIndices[i] + 1];
      } else {
        children[i + 1] = new Identifier("placeholder" + i);
      }
    }
  }

  @Override
  protected void onResolve(ValidationContext resolutionContext, int line) {
    kind = Kind.ERROR;

    Type baseType = children[0].returnType();
    if (baseType instanceof FunctionType) {
      FunctionType functionType = (FunctionType) children[0].returnType();
      resolvedArguments = InvocationResolver.resolve(functionType, children, 1, true, resolutionContext);
      kind = Kind.FUNCTION;
    } else if (baseType instanceof MetaType && ((MetaType) baseType).getWrapped() instanceof InstantiableClassType) {
      InstantiableClassType instantiable = (InstantiableClassType) ((MetaType) baseType).getWrapped();
      resolutionContext.addInstanceDependency(instantiable);
      resolvedArguments = InvocationResolver.resolve(instantiable.getConstructorSignature(), children, 1, false, resolutionContext);
      kind = Kind.CONSTRUCTOR;
    } else {
      throw new RuntimeException("function or class required to apply parameters.");
    }
  }

  public Object eval(EvaluationContext evaluationContext) {
    switch (kind) {
      case FUNCTION:
        Callable function = (Callable) children[0].eval(evaluationContext);
        evaluationContext.ensureExtraStackSpace(function.getLocalVariableCount());
        // Push is important here, as parameter evaluation might also run apply().
        int count = resolvedArguments.length;
        for (int i = 0; i < count; i++) {
          evaluationContext.push(resolvedArguments[i].eval(evaluationContext));
        }
        evaluationContext.popN(count);
        try {
          return function.call(evaluationContext, count);
        } catch (Exception e) {
          throw new RuntimeException(e.getMessage() + " in " + children[0], e);
        }
      case CONSTRUCTOR:
        Object[] values = new Object[resolvedArguments.length];
        for (int i = 0; i < values.length; i++) {
          values[i] = resolvedArguments[i].eval(evaluationContext);
        }
        return ((InstantiableClassType) ((MetaType) (children[0].returnType())).getWrapped()).createInstance(evaluationContext, values);
      default:
        throw new RuntimeException(kind + ": " + this);
    }
  }

  // Shouldn't throw, as it's used outside validation!
  public Type returnType() {
    switch (kind) {
      case FUNCTION:
        return ((FunctionType) children[0].returnType()).getReturnType();
      case CONSTRUCTOR:
        return ((MetaType) (children[0].returnType())).getWrapped();
      default:
        return null;
    }
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    int start = asb.length();
    children[0].toString(asb, errors, preferAscii);
    asb.append("(" , parenthesis ? null : SyntaxColor.HIDE);
    for (int i = 1; i < children.length; i++) {
      if (i > 1) {
        asb.append(", ");
      }
      children[i].toString(asb, errors, preferAscii);
    }
    asb.append(")" , parenthesis ? null : SyntaxColor.HIDE);
    asb.annotate(start, asb.length(), errors.get(this));
  }
}
