package org.kobjects.asde.lang;

import org.kobjects.asde.lang.Types;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Type;

import java.util.Arrays;

public class ArrayType extends FunctionType{

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
        super(elementType, createParameterTypes(elementType));
    }

    public ArrayType(Type elementType, int dimensionality) {
        this(dimensionality == 1 ? elementType : new ArrayType(elementType, dimensionality - 1));
    }
}
