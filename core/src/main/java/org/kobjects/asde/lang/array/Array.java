package org.kobjects.asde.lang.array;

import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.classifier.Method;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Instance;
import org.kobjects.typesystem.Property;
import org.kobjects.typesystem.PropertyDescriptor;
import org.kobjects.typesystem.Type;

import java.util.ArrayList;

public class Array extends Instance {

    private final ArrayList<Object> data;

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
                if (!elementType.hasDefaultValue()) {
                    throw new RuntimeException("Can't create a array of " + elementType + ": type has no default value.");
                }
                for (int i = 0; i < sizes[0]; i++) {
                    data.add(elementType.getDefaultValue());
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
    synchronized public Property getProperty(PropertyDescriptor property) {
        switch (((ArrayType.ArrayPropertyDescriptor) property).propertyEnum) {
            case length: return length;
            case append:  return new Method((FunctionType) property.type()) {
                @Override
                public Object call(EvaluationContext evaluationContext, int paramCount) {
                    append(evaluationContext.getParameter(0));
                    return null;
                }};
            case remove:  return new Method((FunctionType) property.type()) {
                @Override
                public Object call(EvaluationContext evaluationContext, int paramCount) {
                    remove(evaluationContext.getParameter(0));
                    return null;
                }};

            default:
                throw new IllegalArgumentException("Unrecognized property: " + property);
        }
    }


    public synchronized Object get(int index) {
        return data.get(index);
    }

    public synchronized void remove(int index) {
        data.remove(index);
        length.notifyChanged();
    }

    public synchronized void remove(Object object) {
        data.remove(object);
        length.notifyChanged();
    }

    public synchronized void append(Object object) {
        data.add(object);
        length.notifyChanged();
    }


    synchronized public void setValueAt(Object value, int... indices) {
        Array target = this;
        for (int i = 0; i < indices.length - 1; i++) {
            target = (Array) target.data.get(indices[i]);
        }
        target.data.set(indices[indices.length - 1], value);
    }

    @Override
    synchronized public String toString() {
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
    synchronized public boolean equals(Object o) {
        if (!(o instanceof Array)) {
            return false;
        }
        Array other = (Array) o;
        return data.equals(other.data);
    }


    public synchronized int length() {
        return data.size();
    }

    public synchronized boolean contains(Object object) {
        return data.contains(object);
    }
}
