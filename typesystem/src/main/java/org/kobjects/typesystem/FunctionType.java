package org.kobjects.typesystem;

public class FunctionType implements Type {
  private final Type returnType;
  private Type[] parameterTypes;
  private final int minParameterCount;

  public FunctionType(Type returnType, int minParamCount, Type... parameterTypes) {
    this.returnType = returnType;
    this.parameterTypes = parameterTypes;
    this.minParameterCount = minParamCount;
  }

  public FunctionType(Type returnType, Type... parameterTypes) {
    this (returnType, parameterTypes.length, parameterTypes);
  }

  public Type getReturnType() {
    return returnType;
  }

  public Type getParameterType(int index) {
    return parameterTypes[index];
  }

  public int getMinParameterCount() {
    return minParameterCount;
  }
  public int getParameterCount() {
    return parameterTypes.length;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder("(");
    for (int i = 0; i < parameterTypes.length; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(parameterTypes[i].toString());
    }
    sb.append(") -> ");
    sb.append(returnType.toString());
    return sb.toString();
  }

  @Override
  public Type getType() {
    return new MetaType(this);
  }

  @Override
  public boolean hasDefaultValue() {
    return false;
  }

  @Override
  public Object getDefaultValue() {
    throw new UnsupportedOperationException();
  }


  @Override
  public boolean equals(Object o) {
    if (!(o instanceof FunctionType)) {
      System.out.println(this + " does not match " + o+ ": not a function type");
      return false;
    }
    FunctionType other = (FunctionType) o;

    if (!returnType.equals(other.getReturnType())) {
      System.out.println(this + " does not match " + other+ ": return type mismatch");
      return false;
    }
    if (getParameterCount() != other.getParameterCount()) {
      System.out.println(this + " does not match " + other+ ": parameter count mismatch");
      return false;
    }
    if (getMinParameterCount() != other.getMinParameterCount()) {
      System.out.println(this + " does not match " + other+ ": min parameter count mismatch");
      return false;
    }
    for (int i = 0; i < getParameterCount(); i++) {
      if (!getParameterType(i).equals(other.getParameterType(i))) {
        return false;
      }
    }

    return true;
  }


}
