package org.kobjects.asde.lang.type;

import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.LocalStack;
import org.kobjects.typesystem.Type;

import java.util.Arrays;

public class Array implements Function {

    private final ArrayType arrayType;
    private final Object[] data;

    public Array(Type elementType, int... sizes) {
        data = new Object[sizes[0]];
        this.arrayType = new ArrayType(elementType, sizes.length);
        if (sizes.length == 1) {
            Object fillElement;
            if (elementType == Types.NUMBER) {
                fillElement = 0.0;
            } else if (elementType == Types.STRING) {
                fillElement = "";
            } else {
                throw new RuntimeException("Can't create a static array of " + elementType);
            }
            Arrays.fill(data, fillElement);
        } else {
            int[] subSizes = new int[sizes.length - 1];
            System.arraycopy(sizes, 1, subSizes, 0, subSizes.length);
            for (int i = 0; i < sizes[0]; i++) {
                data[i] = new Array(elementType, subSizes);
            }
        }
    }

    public Array(Type elementType, Object[] data) {
        this.arrayType = new ArrayType(elementType);
        this.data = data;
    }

    @Override
    public ArrayType getType() {
        return arrayType;
    }

    @Override
    public Object call(Interpreter interpreter, int paramCount) {
        LocalStack localStack = interpreter.localStack;
        Object result = this;
        for (int i = 0; i < paramCount; i++) {
            result = ((Array) result).data[((Number) localStack.getParameter(i, paramCount)).intValue()];
        }
        return result;
    }

    public void setValueAt(Object value, int... indices) {
        Array target = this;
        for (int i = 0; i < indices.length - 1; i++) {
            target = (Array) target.data[indices[i]];
        }
        target.data[indices[indices.length - 1]] = value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < data.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(data[i]);
        }
        sb.append("}");
        return sb.toString();
    }
}
