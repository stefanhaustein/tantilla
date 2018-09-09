package org.kobjects.asde.lang;

import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Type;

import java.util.Arrays;

public class Array implements Function {

    private final ArrayType arrayType;

    private final Object[] data;

    public Array(Type elementType, int... dimensionalities) {
        this.arrayType = new ArrayType(elementType, dimensionalities);
        int size = 1;
        for (int d : dimensionalities) {
            size *= d;
        }
        data = new Object[size];

        Object fillElement;
        if (elementType == Types.NUMBER) {
            fillElement = 0.0;
        } else if (elementType == Types.STRING) {
            fillElement = "";
        } else {
            throw new RuntimeException("Can't create a static array of " + elementType);
        }

        Arrays.fill(data, fillElement);
    }

    @Override
    public FunctionType getType() {
        return arrayType;
    }

    @Override
    public int getLocalVariableCount() {
        return arrayType.dimensionality.length;
    }

    @Override
    public Object eval(Interpreter interpreter, Object[] args) {
        if (args.length != arrayType.dimensionality.length) {
            throw new RuntimeException("Dimensionality mismatch");
        }

        int index = 0;
        for (int i = 0; i < args.length; i++) {
            index = index * arrayType.dimensionality[i] + ((Number) args[i]).intValue();
        }
        return data[index];
    }

    public void setAt(Object value, int... indices) {
        int index = 0;
        for (int i = 0; i < indices.length; i++) {
            index = index * arrayType.dimensionality[i] + indices[i];
        }
        data[index] = value;
    }
}
