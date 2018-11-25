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
import java.util.Map;

public class ResolutionContext {
    public enum ResolutionMode {FUNCTION, SHELL, MAIN};

    public final Program program;
    public HashMap<Node, Exception> errors = new HashMap<>();
    public final ResolutionMode mode;
    public final FunctionType functionType;

    private HashMap<String, LocalSymbol> localSymbols = new HashMap<>();
    private int depth;
    private int localSymbolCount;


    public ResolutionContext(Program program, ResolutionMode mode, FunctionType type, String... parameterNames) {
        this.program = program;
        this.mode = mode;
        this.functionType = type;
        for (int i = 0; i < parameterNames.length; i++) {
            localSymbols.put(parameterNames[i], new LocalSymbol(localSymbolCount++, type.getParameterType(i), depth));
        }
    }

    public void endBlock() {
        HashMap<String, LocalSymbol> filtered = new HashMap<>();
        for (Map.Entry<String, LocalSymbol> entry : localSymbols.entrySet()) {
            if (entry.getValue().depth < depth) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }
        localSymbols = filtered;
        depth--;
    }

    public void startBlock() {
        depth++;
    }

    public ResolvedSymbol declare(String name, Type type) {
        if (mode != ResolutionMode.FUNCTION) {
            return resolve(name);
        }
        if (localSymbols.containsKey(name)) {
            throw new RuntimeException("Local variable named '" + name + "' already exists");
        }
        LocalSymbol result = new LocalSymbol(localSymbolCount++, type, depth);
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
        return localSymbolCount;
    }
}
