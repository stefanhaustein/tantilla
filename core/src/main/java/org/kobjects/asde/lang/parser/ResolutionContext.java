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
    public enum BlockType {
        ROOT, FOR
    }

    public final Program program;
    public HashMap<Node, Exception> errors = new HashMap<>();
    public final ResolutionMode mode;
    public final FunctionType functionType;

    private int localSymbolCount;
    private Block currentBlock;


    public ResolutionContext(Program program, ResolutionMode mode, FunctionType type, String... parameterNames) {
        this.program = program;
        this.mode = mode;
        this.functionType = type;
        startBlock(BlockType.ROOT);
        for (int i = 0; i < parameterNames.length; i++) {
            currentBlock.localSymbols.put(parameterNames[i], new LocalSymbol(localSymbolCount++, type.getParameterType(i)));
        }
    }

    public void startBlock(BlockType type) {
        currentBlock = new Block(currentBlock, type);
    }

    public void endBlock(BlockType type) {
        if (type != currentBlock.type) {
            throw new RuntimeException((currentBlock.type == BlockType.FOR ? "NEXT" : ("END " + currentBlock.type))
                    + " expected.");
        }
        currentBlock = currentBlock.parent;
    }

    public ResolvedSymbol declare(String name, Type type) {
        if (mode != ResolutionMode.FUNCTION) {
            return resolve(name);
        }
        if (currentBlock.localSymbols.containsKey(name)) {
            throw new RuntimeException("Local variable named '" + name + "' already exists");
        }
        LocalSymbol result = new LocalSymbol(localSymbolCount++, type);
        currentBlock.localSymbols.put(name, result);
        return result;
    }

    public ResolvedSymbol resolve(String name) {
        ResolvedSymbol resolved = currentBlock.get(name);
        if (resolved != null) {
            return resolved;
        }
        GlobalSymbol symbol = program.getSymbol(name);
        switch (mode) {
            case MAIN:
                return symbol != null
                        && (symbol.scope == GlobalSymbol.Scope.PERSISTENT
                           || symbol.scope == GlobalSymbol.Scope.BUILTIN)
                    ? symbol : new DynamicSymbol(name, mode);

            case SHELL:
                return symbol != null && (symbol.scope == GlobalSymbol.Scope.BUILTIN)
                        ? symbol : new DynamicSymbol(name, mode);

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


    private class Block {
        final Block parent;
        final BlockType type;
        private final HashMap<String, LocalSymbol> localSymbols = new HashMap<>();

        Block(Block parent, BlockType blockType) {
            this.parent = parent;
            this.type = blockType;
        }

        LocalSymbol get(String name) {
            LocalSymbol result = localSymbols.get(name);
            return (result == null && parent != null) ? parent.get(name) : result;
        }
    }
}
