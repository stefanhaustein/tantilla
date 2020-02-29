package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.function.Callable;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.list.ListImpl;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.MetaType;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;

// Not static for access to the variables.
public class InvokeMethod extends Node {

  public String name;
  Property resolvedProperty;
  int skipChildren;

  public InvokeMethod(String name, Node... children) {
    super(children);
    this.name = name;
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
  protected void onResolve(ValidationContext resolutionContext, int line) {
    Type baseType = children[0].returnType();

    if (baseType instanceof MetaType && ((MetaType) baseType).getWrapped() instanceof Classifier) {
      skipChildren = 1;
      resolvedProperty = ((Classifier) ((MetaType) baseType).getWrapped()).getProperty(name);
    } else if (baseType instanceof Classifier) {
      skipChildren = 0;
      resolvedProperty = ((Classifier) baseType).getProperty(name);
    } else {
      throw new RuntimeException("Classifier or instance base expected");
    }

    if (resolvedProperty == null) {
      throw new RuntimeException("Property '" + name + "' not found in " + children[0].returnType());
    }
    resolutionContext.validateAndAddDependency(resolvedProperty);

    if (!(resolvedProperty.getType() instanceof FunctionType)) {
      throw new RuntimeException("Type of property '" + resolvedProperty + "' is not callable.");
    }

    FunctionType functionType = (FunctionType) resolvedProperty.getType() ;
    // TODO: b/c optional params, add minParameterCount
    if (children.length - skipChildren > functionType.getParameterCount() || children.length - skipChildren < functionType.getMinParameterCount()) {
      throw new RuntimeException("Expected parameter count is "
          + functionType.getMinParameterCount() + ".."
          + functionType.getParameterCount() + " but got " + (children.length - skipChildren) + " for " + this);
    }
    for (int i = skipChildren; i < children.length; i++) {
      if (!functionType.getParameterType(i - skipChildren).isAssignableFrom(children[i].returnType())) {
        throw new RuntimeException("Type mismatch for parameter " + i + ": expected: "
            + functionType.getParameterType(i) + " actual: " + children[i].returnType() + " base type: " + functionType);
      }
    }
  }

  public Object eval(EvaluationContext evaluationContext) {
    Object base = children[0].eval(evaluationContext);
    Callable function = (Callable) resolvedProperty.get(evaluationContext, base);
    evaluationContext.ensureExtraStackSpace(function.getLocalVariableCount());
    if (children.length > function.getLocalVariableCount()) {
      throw new RuntimeException("Too many params for " + function);
     }
    // Push is important here, as parameter evaluation might also run apply().
    if (skipChildren == 0) {
      evaluationContext.push(base);
    }
    for (int i = 1; i < children.length; i++) {
       evaluationContext.push(children[i].eval(evaluationContext));
     }
     evaluationContext.popN(children.length - skipChildren);
     try {
       return function.call(evaluationContext, children.length - skipChildren);
     } catch (Exception e) {
       throw new RuntimeException(e.getMessage() + " in " + children[0], e);
     }
  }

  // Shouldn't throw, as it's used outside validation!
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
}
