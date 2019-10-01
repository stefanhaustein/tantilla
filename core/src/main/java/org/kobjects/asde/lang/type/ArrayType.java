package org.kobjects.asde.lang.type;

import org.kobjects.typesystem.FunctionTypeImpl;
import org.kobjects.typesystem.InstanceType;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Instance;
import org.kobjects.typesystem.MetaType;
import org.kobjects.typesystem.PropertyDescriptor;
import org.kobjects.typesystem.Type;

import java.util.TreeMap;

public class ArrayType implements FunctionType, InstanceType {

  private final Type elementType;

  public ArrayType(Type elementType) {
    this.elementType = elementType;
  }

  public ArrayType(Type elementType, int dimensionality) {
    this.elementType = dimensionality == 1 ? elementType : new ArrayType(elementType, dimensionality - 1);
  }

  @Override
  public Type getReturnType() {
    return elementType;
  }

  public Type getReturnType(int parameterCount) {
    Type returnType = getReturnType();
    for (int i = 1; i < parameterCount; i++) {
      returnType = ((ArrayType) returnType).getReturnType();
    }
    return returnType;
  }

  @Override
  public Type getParameterType(int index) {
    return Types.NUMBER;
  }

  @Override
  public int getMinParameterCount() {
    return 1;
  }

  @Override
  public int getParameterCount() {
    return elementType instanceof ArrayType ? ((ArrayType) elementType).getParameterCount() + 1 : 1;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ArrayType)) {
      return false;
    }
    return getReturnType().equals(((ArrayType) o).getReturnType());
  }

  @Override
  public String toString() {
    return getReturnType() + "[]";
  }

  @Override
  public Type getType() {
    return new MetaType(this);
  }

  @Override
  public PropertyDescriptor getPropertyDescriptor(String name) {
    switch (name) {
      case "append":
        return new ArrayPropertyDescriptor(ArrayPropertyEnum.append, new FunctionTypeImpl(Types.VOID, elementType));
      case "remove":
        return new ArrayPropertyDescriptor(ArrayPropertyEnum.remove, new FunctionTypeImpl(Types.VOID, elementType));
      case "length":
        return new ArrayPropertyDescriptor(ArrayPropertyEnum.length, Types.NUMBER);
      default:
        throw new IllegalArgumentException("Unrecognized array property: '" + name + "'");
    }
  }

  @Override
  public boolean hasDefaultValue() {
    return false;
  }

  @Override
  public Object getDefaultValue() {
    throw new UnsupportedOperationException();
  }

  class ArrayPropertyDescriptor implements PropertyDescriptor {
    final ArrayPropertyEnum propertyEnum;
    private final Type type;

    ArrayPropertyDescriptor(ArrayPropertyEnum propertyEnum, Type type) {
      this.propertyEnum = propertyEnum;
      this.type = type;
    }

    @Override
    public String name() {
      return propertyEnum.name();
    }

    @Override
    public Type type() {
      return type;
    }
  }

  enum ArrayPropertyEnum {
    append, length, remove
  }
}
