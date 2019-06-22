package org.kobjects.typesystem;

public interface FunctionType extends Type {
  Type getReturnType();

  Type getReturnType(int parameterCount);

  Type getParameterType(int index);

  int getMinParameterCount();
  int getParameterCount();

}
