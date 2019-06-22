package org.kobjects.asde.lang.type;

import org.kobjects.typesystem.Classifier;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.FunctionTypeImpl;
import org.kobjects.typesystem.Instance;
import org.kobjects.typesystem.Type;

public class ArrayType implements FunctionType {

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

}
