package org.kobjects.asde.lang;

import org.kobjects.asde.lang.Types;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Type;

import java.util.Arrays;

public class ArrayType extends FunctionType{

    int dimensionality;

    static Type[] createParameterTypes(int count) {
        Type[] result = new Type[count];
        Arrays.fill(result, Types.NUMBER);
        return result;
    }

    public ArrayType(Type elementType, int dimensionality) {
        super(elementType, createParameterTypes(dimensionality));
        this.dimensionality = dimensionality;
    }
}
