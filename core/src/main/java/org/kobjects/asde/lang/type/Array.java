package org.kobjects.asde.lang.type;

import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Instance;
import org.kobjects.typesystem.Property;
import org.kobjects.typesystem.PropertyDescriptor;
import org.kobjects.typesystem.Type;

import java.util.Arrays;

public class Array extends Instance implements Function {

    private Object[] data;

    private Property<Double> length = new Property<Double>() {
        @Override
        public boolean setImpl(Double aDouble) {
            throw new UnsupportedOperationException("length is read-only");
        }

        @Override
        public Double get() {
            return Double.valueOf(data.length);
        }
    };


    public Array(Array array) {
        super(array.getType());
        this.data = new Object[array.data.length];
        System.arraycopy(array.data, 0, data, 0, array.data.length);
        for (int i = 0; i < data.length; i++) {
            if (data[i] instanceof Array) {
                data[i] = new Array((Array) data[i]);
            }
        }
    }

    public Array(Type elementType, int... sizes) {
        super(new ArrayType(elementType, sizes.length));
        data = new Object[sizes[0]];
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
        super(new ArrayType(elementType));
        this.data = data;
    }

    @Override
    public Property getProperty(PropertyDescriptor property) {
        switch (((ArrayType.ArrayPropertyDescriptor) property).propertyEnum) {
            case length: return length;
            case append:  return new Method((FunctionType) property.type()) {
                @Override
                public Object call(EvaluationContext evaluationContext, int paramCount) {
                    Object[] newData = new Object[data.length + 1];
                    System.arraycopy(data, 0, newData, 0, data.length);
                    newData[data.length] = evaluationContext.getParameter(0);
                    data = newData;
                    return null;
                }};
            default:
                throw new IllegalArgumentException("Unrecognized property: " + property);
        }
    }

    @Override
    public ArrayType getType() {
        return (ArrayType) super.getType();
    }

    @Override
    public Object call(EvaluationContext evaluationContext, int paramCount) {
        Object result = this;
        for (int i = 0; i < paramCount; i++) {
            result = ((Array) result).data[((Number) evaluationContext.getParameter(i)).intValue()];
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Array)) {
            return false;
        }
        Array other = (Array) o;
        if (other.length() != length()) {
            return false;
        }
        for (int i = 0; i < length(); i++) {
            if (!data[i].equals(other.data[i])) {
                return false;
            }
        }
        return true;
    }


    public int length() {
        return data.length;
    }
}
