package org.kobjects.asde.lang.property;

import org.kobjects.asde.lang.function.Function;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;

public abstract class MethodDescriptor implements PropertyDescriptor, Function {
  FunctionType type;
  String name;

  public MethodDescriptor(String name, String description, Type returnType, Type... parameterTypes) {
    this.type = new FunctionType(returnType, parameterTypes);
    this.name = name;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public Type type() {
    return type;
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
  public void addListener(Object instance, PropertyChangeListener listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public FunctionType getType() {
    return type;
  }

  @Override
  public abstract Object call(EvaluationContext evaluationContext, int paramCount);
}
