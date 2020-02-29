package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.function.Callable;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;

public abstract class NativeMethod implements Property, Callable {
  Classifier owner;
  FunctionType type;
  String name;

  public NativeMethod(Classifier owner, String name, String description, Type returnType, Type... parameterTypes) {
    this.owner = owner;
    this.type = new FunctionType(returnType, parameterTypes);
    this.name = name;
  }

  @Override
  public Classifier getOwner() {
    return owner;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Object get(EvaluationContext context, Object instance) {
    return this;
  }

  @Override
  public void set(EvaluationContext context, Object instance, Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public FunctionType getType() {
    return type;
  }

  @Override
  public abstract Object call(EvaluationContext evaluationContext, int paramCount);

  @Override
  public boolean isMutable() {
    return false;
  }

  @Override
  public boolean isInstanceField() {
    return false;
  }

  @Override
  public Object getStaticValue() {
    return this;
  }

  /*
  @Override
  public Map<Node, Exception> getErrors() {
    return Collections.emptyMap();
  }

/*  @Override
  public Node getInitializer() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void validate(ValidationContext parentValidationContext) {
  }
*/
}
