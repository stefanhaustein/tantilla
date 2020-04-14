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

  public static final Node autoCast(Node node, Type expectedType, ValidationContext context) {
    Type type = node.returnType();
    if (expectedType.equals(type)) {
      return node;
    }

    if (type instanceof ClassType && expectedType instanceof Trait) {
      String adapterName = type + " as " + expectedType;
      Property property = context.program.mainModule.getProperty(adapterName);
      if (property == null) {
        throw new RuntimeException("Implementation of " + adapterName + " not found.");
      }
      AdapterType adapterType = (AdapterType) property.getStaticValue();
      context.addInstanceDependency(adapterType);
      return new TraitCast(node, adapterType);
    }

    throw new RuntimeException("Cannot assign value of type " + type + " to expected type " + expectedType);
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
