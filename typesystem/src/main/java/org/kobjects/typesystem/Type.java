package org.kobjects.typesystem;

public interface Type {

    Type NUMBER = new Type() {
        @Override
        public String toString() {
            return "Number";
        }
    };
    Type STRING = new Type() {
        @Override
        public String toString() {
            return "String";
        }
    };
    Type VOID = new Type() {
        @Override
        public String toString() {
            return "Void";
        }

    };
}
