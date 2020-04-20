package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.function.Callable;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.MetaType;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.type.Types;

import java.util.Map;

/**
 * Theoretically, this could be merged with apply using a "dot syntax" flag, but
 */
public class InvokeMethod extends Node {

  public String name;
  boolean isStaticCall;
  Property resolvedProperty;
  Node[] resolvedArguments;

  public InvokeMethod(String name, Node... children) {
    super(children);
    this.name = name;
  }

  @Override
  protected void onResolve(ValidationContext resolutionContext, int line) {
    Type baseType = children[0].returnType();

    if (baseType instanceof MetaType && ((MetaType) baseType).getWrapped() instanceof Classifier) {
      isStaticCall = true;
      resolvedProperty = ((Classifier) ((MetaType) baseType).getWrapped()).getProperty(name);
    } else if (baseType instanceof Classifier) {
      isStaticCall = false;
      resolvedProperty = ((Classifier) baseType).getProperty(name);
    } else {
      throw new RuntimeException("Classifier or instance base expected");
    }

    if (resolvedProperty == null) {
      throw new RuntimeException("Property '" + name + "' not found in " + children[0].returnType());
    }
    resolutionContext.validateProperty(resolvedProperty);

    if (!(resolvedProperty.getType() instanceof FunctionType)) {
      throw new RuntimeException("Type of property '" + resolvedProperty + "' is not callable.");
    }

    FunctionType functionType = (FunctionType) resolvedProperty.getType() ;
    resolvedArguments = InvocationResolver.resolve(functionType, children, isStaticCall ? 1 : 0, true, resolutionContext);
  }

  public Object eval(EvaluationContext evaluationContext) {
    Callable function = (Callable) resolvedProperty.getStaticValue();
      evaluationContext.ensureExtraStackSpace(function.getLocalVariableCount());
      for (int i = 0; i < resolvedArguments.length; i++) {
        evaluationContext.push(resolvedArguments[i].eval(evaluationContext));
      }

     evaluationContext.popN(resolvedArguments.length);
     try {
       Object result = function.call(evaluationContext, resolvedArguments.length);
       if (result == null && returnType() != Types.VOID) {
         throw new NullPointerException("non-void method evaluation of " + function + " returns null");
       }
       return result;
     } catch (Exception e) {
       throw new RuntimeException(e.getMessage() + " in " + this, e);
     }
  }

  // Shouldn't throw, as it's used outside validation!
  public Type returnType() {
    if (resolvedProperty == null) {
      return null;
    }
    FunctionType functionType = (FunctionType) resolvedProperty.getType();
    return functionType == null ? null : functionType.getReturnType();
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    int start = asb.length();
    children[0].toString(asb, errors, preferAscii);
    asb.append(".");
    appendLinked(asb, name, errors);
    asb.append("(");
    for (int i = 1; i < children.length; i++) {
      if (i > 1) {
        asb.append(", ");
      }
      children[i].toString(asb, errors, preferAscii);
    }
    asb.append(")");
    asb.annotate(start, asb.length(), errors.get(this));
  }


  @Override
  public void reorderParameters(Property symbol, int[] oldIndices) {
    if (resolvedProperty != symbol) {
      return;
    }

    Node[] oldChildren = children;
    children = new Node[oldIndices.length];
    for (int i = 0; i < oldIndices.length; i++) {
      if (oldIndices[i] != -1) {
        children[i] = oldChildren[oldIndices[i] + 1];
      } else {
        children[i] = new Identifier("placeholder" + i);
      }
    }
  }

}
