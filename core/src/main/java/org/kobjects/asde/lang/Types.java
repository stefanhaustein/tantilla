package org.kobjects.asde.lang;

import org.kobjects.typesystem.Type;
import org.kobjects.typesystem.Typed;

public class Types {
    public static final Type BOOLEAN = new Type() {
        @Override
        public String toString() {
            return "Boolean";
        }
    };
    public static final Type NUMBER = new Type() {
        @Override
        public String toString() {
            return "Number";
        }
    };
    public static final Type STRING = new Type() {
        @Override
        public String toString() {
            return "String";
        }
    };
    public static final Type VOID = new Type() {
        @Override
        public String toString() {
            return "Void";
        }

    };


    public static Type of(Object o) {
       if (o instanceof Typed) {
           return ((Typed) o).getType();
       }
       if (o instanceof Boolean) {
           return BOOLEAN;
       }
       if (o instanceof Double) {
           return NUMBER;
        }
        if (o instanceof String) {
           return STRING;
        }
        throw new IllegalArgumentException("Unrecognized type: " + o.getClass());
    }

}
