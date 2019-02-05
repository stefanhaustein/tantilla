package org.kobjects.asde.lang.symbol;

import org.kobjects.asde.lang.ArrayType;
import org.kobjects.asde.lang.CallableUnit;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.ProgramValidationContext;
import org.kobjects.asde.lang.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.asde.lang.statement.DimStatement;
import org.kobjects.asde.lang.statement.LetStatement;
import org.kobjects.typesystem.Type;

import java.util.HashMap;
import java.util.HashSet;

public class GlobalSymbol implements ResolvedSymbol {

    public int stamp;
    public Node initializer;
    public Object value;
    public Scope scope;
    public Type type;
    boolean immutable;
    public HashMap<Node, Exception> errors = new HashMap<>();
    public HashSet<GlobalSymbol> dependencies;


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


    public GlobalSymbol(Scope scope, Object value) {
        this.scope = scope;
        this.value = value;
        this.type = value == null ? null : Types.of(value);
    }


    public String toString(String name, boolean showValue) {
        if (initializer == null) {
            return name+ " = " + value;
        }
        return initializer.toString() + (showValue && value != null ? (" ' " + value) : "");
    }


    public void validate(ProgramValidationContext programValidationContext) {
        if (programValidationContext.validated.contains(this)) {
            return;
        }
        if (value instanceof CallableUnit) {
            FunctionValidationContext context = ((CallableUnit) value).validate(programValidationContext);
            this.errors = context.errors;
            this.dependencies = context.dependencies;
        } else if (initializer != null) {
            FunctionValidationContext context = new FunctionValidationContext(programValidationContext, FunctionValidationContext.ResolutionMode.SHELL, null);
            initializer.resolve(context, 0, 0);
            this.errors = context.errors;
            if (initializer instanceof DimStatement) {
                DimStatement dimStatement = (DimStatement) initializer;
                Type elementType = dimStatement.varName.endsWith("$") ? Types.STRING : Types.NUMBER;
                type = new ArrayType(elementType, dimStatement.children.length);
            } else if (errors.size() > 0) {
                type = null;
            } else if (initializer instanceof LetStatement) {
                type = initializer.children[0].returnType();
            } else {
                throw new RuntimeException("not an initializer statement: " + initializer);
            }
            this.dependencies = context.dependencies;
        }
        programValidationContext.validated.add(this);
    }

    public void init(Interpreter interpreter, HashSet<GlobalSymbol> initialized) {
        if (initialized.contains(this)) {
            return;
        }
        if (dependencies != null) {
            for (GlobalSymbol dep : dependencies) {
                dep.init(interpreter, initialized);
            }
        }
        if (initializer != null) {
            initializer.eval(interpreter);
        }
        initialized.add(this);
    }
}
