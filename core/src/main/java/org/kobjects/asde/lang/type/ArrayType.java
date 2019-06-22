package org.kobjects.asde.lang.type;

import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.FunctionTypeImpl;
import org.kobjects.typesystem.Type;

public class ArrayType extends FunctionTypeImpl {

    static Type[] createParameterTypes(Type type) {
        Type[] result;
        if (type instanceof ArrayType) {
            Type[] inner = createParameterTypes(((ArrayType) type).getReturnType());
            result = new Type[inner.length + 1];
            System.arraycopy(inner, 0, result, 1, inner.length);
        } else {
            result = new Type[1];
        }
        result[0] = Types.NUMBER;
        return result;
    }

    public ArrayType(Type elementType) {
        super(elementType, 1, createParameterTypes(elementType));
    }

    public ArrayType(Type elementType, int dimensionality) {
        this(dimensionality == 1 ? elementType : new ArrayType(elementType, dimensionality - 1));
    }

    public Type getReturnType(int parameterCount) {
        Type returnType = getReturnType();
        for (int i = 1; i < parameterCount; i++) {
            returnType = ((ArrayType) returnType).getReturnType();
        }
        return returnType;
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
