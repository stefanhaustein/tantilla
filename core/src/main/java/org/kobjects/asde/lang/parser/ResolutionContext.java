package org.kobjects.asde.lang.parser;


import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.symbol.DynamicSymbol;
import org.kobjects.asde.lang.symbol.GlobalSymbol;
import org.kobjects.asde.lang.symbol.LocalSymbol;
import org.kobjects.asde.lang.symbol.ResolvedSymbol;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Type;

import java.util.HashMap;

public class ResolutionContext {
    public enum ResolutionMode {FUNCTION, SHELL, MAIN};

    Program program;
    public HashMap<Node, Exception> errors = new HashMap<>();
    HashMap<String, LocalSymbol> localSymbols = new HashMap<>();
    ResolutionMode mode;

    public ResolutionContext(Program program, ResolutionMode mode, FunctionType type, String... parameterNames) {
        this.program = program;
        this.mode = mode;
        for (int i = 0; i < parameterNames.length; i++) {
            localSymbols.put(parameterNames[i], new LocalSymbol(i, type.getParameterType(i)));
        }
    }

    public ResolvedSymbol declare(String name, Type type) {
        if (mode != ResolutionMode.FUNCTION) {
            return resolve(name);
        }
        if (localSymbols.containsKey(name)) {
            throw new RuntimeException("Local variable named '" + name + "' already exists");
        }
        LocalSymbol result = new LocalSymbol(localSymbols.size(), type);
        localSymbols.put(name, result);
        return result;
    }

    public ResolvedSymbol resolve(String name) {
        ResolvedSymbol resolved = localSymbols.get(name);
        if (resolved != null) {
            return resolved;
        }
        GlobalSymbol symbol = program.getSymbol(name);
        switch (mode) {
            case MAIN:
                return symbol != null && symbol.scope == GlobalSymbol.Scope.PERSISTENT ? symbol : new DynamicSymbol(name, mode);

            case SHELL:
                return new DynamicSymbol(name, mode);

            default:
                if (symbol == null || symbol.scope == GlobalSymbol.Scope.TRANSIENT) {
                    throw new RuntimeException("Variable not found: \"" + name + "\"");
                }
                return symbol;
        }
    }

    public void addError(Node node, Exception e) {
        errors.put(node, e);
    }

    public int getLocalVariableCount() {
        return localSymbols.size();
    }
}
