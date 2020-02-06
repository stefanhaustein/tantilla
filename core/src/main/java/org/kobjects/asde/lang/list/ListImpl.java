package org.kobjects.asde.lang.list;

import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.classifier.Method;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.classifier.Instance;
import org.kobjects.asde.lang.property.Property;
import org.kobjects.asde.lang.property.PropertyDescriptor;
import org.kobjects.asde.lang.type.Type;

import java.util.ArrayList;
import java.util.Iterator;

public class ListImpl extends Instance implements Iterable<Object> {

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


    public ListImpl(ListImpl array) {
        super(array.getType());
        this.data = new ArrayList<>(array.length());
        for (int i = 0; i < data.size(); i++) {
            Object value = array.data.get(i);
            data.add(value instanceof ListImpl ? new ListImpl((ListImpl) value) : value);
       }
    }

    public ListImpl(Type elementType, int... sizes) {
        super(new ListType(elementType, sizes.length));
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
                data.add(new ListImpl(elementType, subSizes));
            }
        }
    }

    public ListImpl(Type elementType, Object[] data) {
        super(new ListType(elementType));
        this.data = new ArrayList<>(data.length);
        for (Object value : data) {
            this.data.add(value);
        }
    }

    @Override
    synchronized public Property getProperty(PropertyDescriptor property) {
        switch (((ListType.ArrayPropertyDescriptor) property).propertyEnum) {
            case size: return length;
            case append:  return new Method((FunctionType) property.type()) {
                @Override
                public Object call(EvaluationContext evaluationContext, int paramCount) {
                    append(evaluationContext.getParameter(0));
                    return null;
                }};
            case clear:  return new Method((FunctionType) property.type()) {
                @Override
                public Object call(EvaluationContext evaluationContext, int paramCount) {
                    clear();
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


    public synchronized void clear() {
        data.clear();
        length.notifyChanged();
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
        ListImpl target = this;
        for (int i = 0; i < indices.length - 1; i++) {
            target = (ListImpl) target.data.get(indices[i]);
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
        if (!(o instanceof ListImpl)) {
            return false;
        }
        ListImpl other = (ListImpl) o;
        return data.equals(other.data);
    }


    public synchronized int length() {
        return data.size();
    }

    public synchronized boolean contains(Object object) {
        return data.contains(object);
    }

    public Iterator<Object> iterator() {
        return data.iterator();
    }
}
