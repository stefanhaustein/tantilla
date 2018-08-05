package org.kobjects.asde.lang;

import org.kobjects.asde.lang.type.Type;

public class Symbol {

    Type type;
    public Object value;
    boolean persistent;
    boolean immutable;


    public Symbol(Object value) {
        this.value = value;
    }

}
