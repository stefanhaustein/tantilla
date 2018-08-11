package org.kobjects.asde.lang;

import org.kobjects.typesystem.Type;

public class Types {
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
}
