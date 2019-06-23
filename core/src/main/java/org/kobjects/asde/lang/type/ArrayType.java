package org.kobjects.asde.lang.type;

import org.kobjects.typesystem.InstanceType;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Instance;
import org.kobjects.typesystem.MetaType;
import org.kobjects.typesystem.PropertyDescriptor;
import org.kobjects.typesystem.Type;

import java.util.TreeMap;

public class ArrayType implements FunctionType, InstanceType {

    static final TreeMap<String,PropertyDescriptor> PROPERTIES = new TreeMap<>();
    static {
        for(ArrayMetaProperty metaProperty : ArrayMetaProperty.values()) {
            PROPERTIES.put(metaProperty.name(), metaProperty);
        }
    }

    private final Type elementType;

    public ArrayType(Type elementType) {
        this.elementType = elementType;
    }

    public ArrayType(Type elementType, int dimensionality) {
        this.elementType = dimensionality == 1 ? elementType : new ArrayType(elementType, dimensionality - 1);
    }

    @Override
    public Type getReturnType() {
        return elementType;
    }

    public Type getReturnType(int parameterCount) {
        Type returnType = getReturnType();
        for (int i = 1; i < parameterCount; i++) {
            returnType = ((ArrayType) returnType).getReturnType();
        }
        return returnType;
    }

    @Override
    public Type getParameterType(int index) {
        return Types.NUMBER;
    }

    @Override
    public int getMinParameterCount() {
        return 1;
    }

    @Override
    public int getParameterCount() {
        return elementType instanceof ArrayType ? ((ArrayType) elementType).getParameterCount() + 1 : 1;
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

    @Override
    public Type getType() {
        return new MetaType(this);
    }

    @Override
    public PropertyDescriptor getPropertyDescriptor(String name) {
        return PROPERTIES.get(name);
    }

    enum ArrayMetaProperty implements PropertyDescriptor {
        length(Types.NUMBER);

        private final Type type;

        ArrayMetaProperty(Type type) {
            this.type = type;
        }

        @Override
        public Type type() {
            return type;
        }
    }
}
