package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.function.Function;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;

public abstract class NativeMethodDescriptor implements PropertyDescriptor, Function {
  FunctionType type;
  String name;

  public NativeMethodDescriptor(String name, String description, Type returnType, Type... parameterTypes) {
    this.type = new FunctionType(returnType, parameterTypes);
    this.name = name;
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
  public boolean isConstant() {
    return true;
  }
}
