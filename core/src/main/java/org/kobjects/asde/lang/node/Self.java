package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.classifier.ClassImplementation;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.classifier.ClassPropertyDescriptor;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;

public class Self extends Node {

  ClassImplementation resolvedType;

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, int line) {
    if (!resolutionContext.functionImplementation.isMethod()) {
      throw new RuntimeException("'self' is only valid inside methods.");
    }
    ClassPropertyDescriptor propertyDescriptor = (ClassPropertyDescriptor) resolutionContext.functionImplementation.getDeclaringSymbol();
    resolvedType = propertyDescriptor.getOwner();
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    return evaluationContext.self;
  }

  @Override
  public Type returnType() {
    return resolvedType;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, "self", errors);
  }
}
