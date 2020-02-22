package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.function.Function;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Collections;
import java.util.Map;

public abstract class NativeMethod implements Property, Function {
  FunctionType type;
  String name;

  public NativeMethod(String name, String description, Type returnType, Type... parameterTypes) {
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

  @Override
  public Map<Node, Exception> getErrors() {
    return Collections.emptyMap();
  }
}
