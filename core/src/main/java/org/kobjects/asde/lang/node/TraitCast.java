package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.classifier.AdapterInstance;
import org.kobjects.asde.lang.classifier.AdapterType;
import org.kobjects.asde.lang.classifier.ClassInstance;
import org.kobjects.asde.lang.classifier.ClassType;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.classifier.Trait;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;

public class TraitCast extends Node {

  public static AdapterType getAdapterType(Type actualType, Type expectedType, ValidationContext validationContext) {
    if (expectedType.equals(actualType)) {
      return null;
    }

    if (actualType instanceof ClassType && expectedType instanceof Trait) {
      String adapterName = actualType + " as " + expectedType;
      Property property = validationContext.program.mainModule.getProperty(adapterName);
      if (property == null) {
        throw new RuntimeException("Implementation of " + adapterName + " not found.");
      }
      AdapterType adapterType = (AdapterType) property.getStaticValue();
      validationContext.addInstanceDependency(adapterType);
      return adapterType;
    }
    throw new RuntimeException("Cannot assign value of type " + actualType + " to expected type " + expectedType);

  }

  public static final Node autoCast(Node node, Type expectedType, ValidationContext context) {
    AdapterType adapterType = getAdapterType(node.returnType(), expectedType, context);

    return adapterType == null ? node : new TraitCast(node, adapterType);

  }


  private final AdapterType adapterType;

  TraitCast(Node instanceExpression, AdapterType adapterType) {
    super(instanceExpression);
    this.adapterType = adapterType;
  }

  @Override
  protected void onResolve(ValidationContext resolutionContext, int line) {

  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    return new AdapterInstance(adapterType, (ClassInstance) children[0].eval(evaluationContext));
  }

  @Override
  public Type returnType() {
    return adapterType.trait;
  }
}
