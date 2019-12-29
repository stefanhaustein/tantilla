package org.kobjects.asde.lang.type;

import org.kobjects.typesystem.FunctionTypeImpl;
import org.kobjects.typesystem.InstanceType;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Instance;
import org.kobjects.typesystem.MetaType;
import org.kobjects.typesystem.PropertyDescriptor;
import org.kobjects.typesystem.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

public class ArrayType implements InstanceType {

  public final Type elementType;

  public ArrayType(Type elementType) {
    if (elementType == null) {
      throw new RuntimeException("ElementType must not be null");
    }
    this.elementType = elementType;
  }

  public ArrayType(Type elementType, int dimensionality) {
    this.elementType = dimensionality == 1 ? elementType : new ArrayType(elementType, dimensionality - 1);
  }


  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ArrayType)) {
      return false;
    }
    return elementType.equals(((ArrayType) o).elementType);
  }

  @Override
  public String toString() {
    return elementType + "[]";
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
  public Collection<? extends PropertyDescriptor> getPropertyDescriptors() {
    ArrayList<PropertyDescriptor> result = new ArrayList<>();
    result.add(getPropertyDescriptor("append"));
    result.add(getPropertyDescriptor("remove"));
    result.add(getPropertyDescriptor("length"));
    return result;
  }

  @Override
  public CharSequence getDocumentation() {
    return null;
  }

  @Override
  public boolean hasDefaultValue() {
    return false;
  }

  @Override
  public Object getDefaultValue() {
    throw new UnsupportedOperationException();
  }

  public int getDimension() {
    int dim = 1;
    Type type = elementType;
    while (type instanceof ArrayType) {
      type = ((ArrayType) type).elementType;
      dim++;
    }
    return dim;
  }

  public Type getRootElementType() {
    return getElementType(getDimension() - 1);
  }

  public Type getElementType(int dim) {
    Type type = elementType;
    for (int i = 1; i < dim; i++) {
      type = ((ArrayType) type).elementType;
    }
    return type;
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
