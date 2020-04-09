package org.kobjects.asde.lang.function;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.type.MetaType;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.type.Types;

public class FunctionType implements Type {
  private final Type returnType;
  private Parameter[] parameters;
  private final int minParameterCount;

  public FunctionType(Type returnType, int minParameterCount, Parameter... parameters) {
    this.returnType = returnType;
    this.parameters = parameters;
    this.minParameterCount = minParameterCount;
  }

  public FunctionType(Type returnType, int minParamCount, Type... parameterTypes) {
    this.returnType = returnType;
    this.parameters = new Parameter[parameterTypes.length];
    for (int i = 0; i < parameters.length; i++) {
      parameters[i] = Parameter.create(String.valueOf(((char) ('a' + i))), parameterTypes[i]);
    }
    this.minParameterCount = minParamCount;
  }

  public FunctionType(Type returnType, Type... parameterTypes) {
    this (returnType, parameterTypes.length, parameterTypes);
  }

  public Type getReturnType() {
    return returnType;
  }

  public Type getParameterType(int index) {
    return parameters[index].type;
  }

  public int getMinParameterCount() {
    return minParameterCount;
  }
  public int getParameterCount() {
    return parameters.length;
  }

  public String toString() {
    AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
    toString(asb);
    return asb.toString();
  }

  public void toString(AnnotatedStringBuilder asb) {
    asb.append('(');
    for (int i = 0; i < parameters.length; i++) {
      if (i > 0) {
        asb.append(", ");
      }
      asb.append(parameters[i].toString());
    }
    if (returnType != Types.VOID) {
      asb.append(") -> ");
      asb.append(returnType.toString());
    }
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
    return this.isAssignableFrom(other) && other.isAssignableFrom(this);
  }

  public boolean isAssignableFromType(Type otherType) {
    if (!(otherType instanceof FunctionType)) {
      return false;
    }
    return isAssignableFrom((FunctionType) otherType, false);
  }

  public boolean isAssignableFrom(FunctionType other, boolean skip0) {
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
    for (int i = skip0 ? 1 : 0; i < getParameterCount(); i++) {
      if (!getParameterType(i).equals(other.getParameterType(i))) {
        return false;
      }
    }

    return true;
  }


  public Parameter getParameter(int i) {
    return parameters[i];
  }
}
