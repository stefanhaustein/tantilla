package org.kobjects.asde.lang.type;

import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Instance;
import org.kobjects.typesystem.Property;
import org.kobjects.typesystem.PropertyDescriptor;
import org.kobjects.typesystem.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Array extends Instance implements Function {

    public final ArrayList<Object> data;

    public Property<Double> length = new Property<Double>() {
        @Override
        public boolean setImpl(Double aDouble) {
            throw new UnsupportedOperationException("length is read-only");
        }

        @Override
        public Double get() {
            return Double.valueOf(data.size());
        }
    };


    public Array(Array array) {
        super(array.getType());
        this.data = new ArrayList<>(array.length());
        for (int i = 0; i < data.size(); i++) {
            Object value = array.data.get(i);
            data.add(value instanceof Array ? new Array((Array) value) : value);
       }
    }

    public Array(Type elementType, int... sizes) {
        super(new ArrayType(elementType, sizes.length));
        data = new ArrayList<>(sizes[0]);
        if (sizes.length == 1) {
            if (sizes[0] > 0) {
                Object fillElement;
                if (elementType == Types.NUMBER) {
                    fillElement = 0.0;
                } else if (elementType == Types.STRING) {
                    fillElement = "";
                } else {
                    throw new RuntimeException("Can't create a static array of " + elementType);
                }
                for (int i = 0; i < sizes[0]; i++) {
                    data.add(fillElement);
                }
            }
        } else {
            int[] subSizes = new int[sizes.length - 1];
            System.arraycopy(sizes, 1, subSizes, 0, subSizes.length);
            for (int i = 0; i < sizes[0]; i++) {
                data.add(new Array(elementType, subSizes));
            }
        }
    }

    public Array(Type elementType, Object[] data) {
        super(new ArrayType(elementType));
        this.data = new ArrayList<>(data.length);
        for (Object value : data) {
            this.data.add(value);
        }
    }

    @Override
    public Property getProperty(PropertyDescriptor property) {
        switch (((ArrayType.ArrayPropertyDescriptor) property).propertyEnum) {
            case length: return length;
            case append:  return new Method((FunctionType) property.type()) {
                @Override
                public Object call(EvaluationContext evaluationContext, int paramCount) {
                    data.add(evaluationContext.getParameter(0));
                    length.notifyChanged();
                    return null;
                }};
            case remove:  return new Method((FunctionType) property.type()) {
                @Override
                public Object call(EvaluationContext evaluationContext, int paramCount) {
                    data.remove(evaluationContext.getParameter(0));
                    length.notifyChanged();
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
            result = ((Array) result).data.get(((Number) evaluationContext.getParameter(i)).intValue());
        }
        return result;
    }

    public void setValueAt(Object value, int... indices) {
        Array target = this;
        for (int i = 0; i < indices.length - 1; i++) {
            target = (Array) target.data.get(indices[i]);
        }
        target.data.set(indices[indices.length - 1], value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < data.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(data.get(i));
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
        return data.equals(other.data);
    }


    public int length() {
        return data.size();
    }
}
