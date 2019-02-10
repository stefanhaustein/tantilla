package org.kobjects.asde.lang;


import org.kobjects.asde.lang.node.Node;
import org.kobjects.typesystem.Type;

import java.util.HashMap;
import java.util.HashSet;

public class FunctionValidationContext {
    public enum ResolutionMode {STRICT, SHELL, LEGACY};
    public enum BlockType {
        ROOT, FOR, IF
    }

    public final Program program;
    public HashMap<Node, Exception> errors = new HashMap<>();
    public final ResolutionMode mode;
    public final FunctionImplementation functionImplementation;

    private int localSymbolCount;
    private Block currentBlock;
    public HashSet<GlobalSymbol> dependencies = new HashSet<>();
    private final ProgramValidationContext programValidationContext;

    public FunctionValidationContext(ProgramValidationContext programValidationContext, ResolutionMode mode, FunctionImplementation functionImplementation, String... parameterNames) {
        this.programValidationContext = programValidationContext;
        this.program = programValidationContext.program;
        this.mode = mode;
        this.functionImplementation = functionImplementation;
        startBlock(BlockType.ROOT);
        for (int i = 0; i < parameterNames.length; i++) {
            currentBlock.localSymbols.put(parameterNames[i], new LocalSymbol(localSymbolCount++, functionImplementation.getType().getParameterType(i), false));
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

    public ResolvedSymbol declare(String name, Type type, boolean constant) {
        if (mode != ResolutionMode.STRICT) {
            return resolve(name, true);
        }
        if (currentBlock.localSymbols.containsKey(name)) {
            throw new RuntimeException("Local variable named '" + name + "' already exists");
        }
        LocalSymbol result = new LocalSymbol(localSymbolCount++, type, constant);
        currentBlock.localSymbols.put(name, result);
        return result;
    }

    public ResolvedSymbol resolve(String name) {
        return resolve(name, false);
    }

    public ResolvedSymbol resolve(String name, boolean forDeclaration) {
        ResolvedSymbol resolved = currentBlock.get(name);
        if (resolved != null) {
            return resolved;
        }
        GlobalSymbol symbol = forDeclaration ? program.getSymbol(name) : programValidationContext.resolve(name);
        switch (mode) {
            case LEGACY:
                if (symbol != null
                        && (symbol.scope == GlobalSymbol.Scope.PERSISTENT
                           || symbol.scope == GlobalSymbol.Scope.BUILTIN)) {
                    dependencies.add(symbol);
                    return symbol;
                }
                return new DynamicSymbol(name, mode);

            case SHELL:
                if (symbol != null && (symbol.scope != GlobalSymbol.Scope.TRANSIENT)) {
                    if (!forDeclaration) {
                        dependencies.add(symbol);
                    }
                    return symbol;
                }
                return new DynamicSymbol(name, mode);

            default:
                if (symbol == null || symbol.scope == GlobalSymbol.Scope.TRANSIENT) {
                    throw new RuntimeException("Variable not found: \"" + name + "\"");
                }
                dependencies.add(symbol);
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
