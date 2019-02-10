package org.kobjects.asde.lang;

import org.kobjects.asde.lang.type.ArrayType;
import org.kobjects.asde.lang.type.FunctionImplementation;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.statement.DimStatement;
import org.kobjects.asde.lang.statement.LetStatement;
import org.kobjects.typesystem.Type;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GlobalSymbol implements ResolvedSymbol {

    public enum Scope {
        BUILTIN,
        PERSISTENT,
        TRANSIENT
    }

    private final Program program;
    private String name;
    Node initializer;
    Object value;
    Scope scope;
    Type type;
    boolean immutable;
    private Map<Node, Exception> errors = Collections.emptyMap();
    Set<GlobalSymbol> dependencies = Collections.emptySet();

    GlobalSymbol(Program program, String name, Scope scope, Object value) {
        this.program = program;
        this.name = name;
        this.scope = scope;
        this.value = value;
        this.type = value == null ? null : Types.of(value);
    }

    @Override
    public Object get(Interpreter interpreter) {
        return value;
    }

    public Map<Node, Exception> getErrors() {
        return errors;
    }

    public Node getInitializer() {
        return initializer;
    }

    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return type;
    }

    public Scope getScope() {
        return scope;
    }

    public Object getValue() {
        return value;
    }

    void init(Interpreter interpreter, HashSet<GlobalSymbol> initialized) {
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

    @Override
    public void set(Interpreter interpreter, Object value) {
        this.value = value;
    }

    void setName(String name) {
        this.name = name;
    }

    public String toString(boolean showValue) {
        if (initializer == null) {
            return name+ " = " + value;
        }
        return initializer.toString() + (showValue && value != null ? (" ' " + value) : "");
    }

    void validate() {
        ProgramValidationContext context = new ProgramValidationContext(program);
        context.startChain(name);
        validate(context);
    }

    void validate(ProgramValidationContext programValidationContext) {
        if (programValidationContext.validated.contains(this)) {
            return;
        }
        if (value instanceof FunctionImplementation) {
            FunctionValidationContext context = ((FunctionImplementation) value).validate(programValidationContext);
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

}
