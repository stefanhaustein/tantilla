package org.kobjects.asde.lang.symbol;

import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.typesystem.Type;

public class GlobalSymbol implements ResolvedSymbol {

    public Node initializer;

    @Override
    public Object get(Interpreter interpreter) {
        return value;
    }

    @Override
    public void set(Interpreter interpreter, Object value) {
        this.value = value;
    }

    @Override
    public Type getType() {
        return type;
    }

    public enum Scope {
        BUILTIN,
        PERSISTENT,
        TRANSIENT
    }

    public Type type;
    public Object value;
    public Scope scope;
    boolean immutable;


    public GlobalSymbol(Scope scope, Object value) {
        this.scope = scope;
        this.value = value;
    }

}
