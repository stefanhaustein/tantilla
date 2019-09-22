package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.ClassImplementation;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class Self extends Node {

  ClassImplementation resolvedType;

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, int line, int index) {
    if (!resolutionContext.functionImplementation.isMethod()) {
      throw new RuntimeException("'self' is only valid inside methods.");
    }
    ClassImplementation.ClassPropertyDescriptor propertyDescriptor = (ClassImplementation.ClassPropertyDescriptor) resolutionContext.functionImplementation.getDeclaringSymbol();
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
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
    appendLinked(asb, "self", errors);
  }
}
