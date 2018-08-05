package org.kobjects.asde.lang;

import org.kobjects.asde.lang.type.Type;

public class Symbol {
    public enum Scope {
        BUILTIN,
        PERSISTENT,
        TRANSIENT
    }

    Type type;
    public Object value;
    public Scope scope;
    boolean immutable;


    public Symbol(Scope scope, Object value) {
        this.scope = scope;
        this.value = value;
    }

}
