package org.kobjects.asde.lang;

import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Type;

import java.util.Arrays;

public class Array implements Function {

    private final ArrayType arrayType;
    private final Object[] data;
    private int[] dimensionalities;

    public Array(Type elementType, int... dimensionalities) {
        this.arrayType = new ArrayType(elementType, dimensionalities.length);
        int size = 1;
        this.dimensionalities = dimensionalities;
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
    public ArrayType getType() {
        return arrayType;
    }


    @Override
    public Object call(Interpreter interpreter, int paramCount) {
        if (paramCount != arrayType.dimensionality) {
            throw new RuntimeException("Dimensionality mismatch");
        }

        LocalStack localStack = interpreter.localStack;
        int index = 0;
        for (int i = 0; i < paramCount; i++) {
            index = index * dimensionalities[i] + ((Number) localStack.getParameter(i, paramCount)).intValue();
        }
        return data[index];
    }

    public void setValueAt(Object value, int... indices) {
        if (indices.length != dimensionalities.length) {
            throw new RuntimeException("Dimensionality mismatch");
        }
        int index = 0;
        for (int i = 0; i < indices.length; i++) {
            index = index * dimensionalities[i] + indices[i];
        }
        data[index] = value;
    }
}
