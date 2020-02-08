package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.classifier.Instance;
import org.kobjects.asde.lang.classifier.InstanceType;
import org.kobjects.asde.lang.function.Function;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.asde.lang.io.SyntaxColor;
import org.kobjects.asde.lang.list.ListImpl;
import org.kobjects.asde.lang.property.PropertyDescriptor;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.symbol.StaticSymbol;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;

// Not static for access to the variables.
public class InvokeMethod extends Node {

  String name;
  PropertyDescriptor resolvedPropertyDescriptor;

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
  protected void onResolve(FunctionValidationContext resolutionContext, int line) {
    if (!(children[0].returnType() instanceof InstanceType)) {
      throw new RuntimeException("InstanceType base expected");
    }
    resolvedPropertyDescriptor = ((InstanceType) children[0].returnType()).getPropertyDescriptor(name);
    if (resolvedPropertyDescriptor == null) {
      throw new RuntimeException("Property '" + name + "' not found in " + children[0].returnType());
    }
    if (!(resolvedPropertyDescriptor.type() instanceof FunctionType)) {
      throw new RuntimeException("Type of property '" + resolvedPropertyDescriptor + "' is not callable.");
    }

    FunctionType resolved = (FunctionType) resolvedPropertyDescriptor.type() ;
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
    Function function = (Function) resolvedPropertyDescriptor.get(evaluationContext, base);
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
    return ((FunctionType) resolvedPropertyDescriptor.type()).getReturnType();
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
