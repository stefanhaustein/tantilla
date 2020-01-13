package org.kobjects.asde.lang.function;


import org.kobjects.asde.lang.program.GlobalSymbol;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.program.ProgramValidationContext;
import org.kobjects.asde.lang.symbol.ResolvedSymbol;
import org.kobjects.asde.lang.classifier.ClassValidationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.array.ArrayType;
import org.kobjects.typesystem.Type;

import java.util.HashMap;
import java.util.HashSet;

public class FunctionValidationContext {
  public enum ResolutionMode {PROGRAM, INTERACTIVE, LEGACY};
  public enum BlockType {
    ROOT, FOR, IF
  }

  public final Program program;
  public HashMap<Node, Exception> errors = new HashMap<>();
  public final ResolutionMode mode;

  /** Will be null when validating symbols! */
  public final FunctionImplementation functionImplementation;

  private int localSymbolCount;
  private Block currentBlock;
  public HashSet<GlobalSymbol> dependencies = new HashSet<>();
  public final ProgramValidationContext programValidationContext;
  private final ClassValidationContext classValidationContext;

  private FunctionValidationContext(ProgramValidationContext programValidationContext, ClassValidationContext classValidationContext, ResolutionMode mode, FunctionImplementation functionImplementation) {
    this.programValidationContext = programValidationContext;
    this.classValidationContext = classValidationContext;
    this.program = programValidationContext.program;
    this.mode = (program.isLegacyMode() && functionImplementation == program.main) ? ResolutionMode.LEGACY : mode;
    this.functionImplementation = functionImplementation;
    startBlock(BlockType.ROOT);
    if (functionImplementation != null) {
      for (int i = 0; i < functionImplementation.parameterNames.length; i++) {
        currentBlock.localSymbols.put(functionImplementation.parameterNames[i], new LocalSymbol(localSymbolCount++, functionImplementation.getType().getParameterType(i), false));
      }
    }
  }

  public FunctionValidationContext(ProgramValidationContext programValidationContext, ResolutionMode mode, FunctionImplementation functionImplementation) {
    this (programValidationContext, null, mode, functionImplementation);
  }

  public FunctionValidationContext(ClassValidationContext classValidationContext, FunctionImplementation functionImplementation) {
    this (classValidationContext.programValidationContext, classValidationContext, ResolutionMode.PROGRAM, functionImplementation);
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


  public ResolvedSymbol resolveVariableDeclaration(String name, Type type, boolean constant) {
    if (mode != ResolutionMode.PROGRAM) {
      return resolveVariableAssignment(name, type, false);
    }
    if (currentBlock.localSymbols.containsKey(name)) {
      throw new RuntimeException("Local variable named '" + name + "' already exists");
    }
    LocalSymbol result = new LocalSymbol(localSymbolCount++, type, constant);
    currentBlock.localSymbols.put(name, result);
    return result;
  }

  public ResolvedSymbol resolveVariableAssignment(String name, Type type) {
    return resolveVariableAssignment(name, type, true);
  }

  private ResolvedSymbol resolveVariableAssignment(String name, Type type, boolean addDependency) {

    ResolvedSymbol resolved = resolveVariableAccess(name, type, addDependency, true);
    // Doesn't check for assigning to constants here because this is also called from
    // resolveVariableDeclaration handling the initial assignment, so that part is checked in
    // AssignStatement.


    if (resolved.getType() != null && !resolved.getType().isAssignableFrom(type)) {
      throw new RuntimeException("Cannot assign value of type " + type + " to " + name + " of type " + resolved.getType());
    }

    return resolved;
  }

  public ResolvedSymbol resolveVariableAccess(String name, Type impliedType) {
    return resolveVariableAccess(name, impliedType, true, false);
  }

  private ResolvedSymbol resolveVariableAccess(String name, Type impliedType, boolean addDependency, boolean forAssignment) {

    // Block level

    // System.out.println("resolveVariableAccess " + (classValidationContext == null ? "" : (classValidationContext.classImplementation + ".")) + name);

    ResolvedSymbol resolved = currentBlock.get(name);
    if (resolved != null) {
      return resolved;
    }

    // Members

    if (classValidationContext != null) {
      resolved = classValidationContext.resolve(name);
      if (resolved != null) {
        return resolved;
      }
    }

    // Globals

    if (mode == ResolutionMode.LEGACY
        && impliedType instanceof ArrayType
        && (program.getSymbol(name) == null
          || program.getSymbol(name).scope == GlobalSymbol.Scope.TRANSIENT)) {
      name += "[" + ((ArrayType) impliedType).getDimension() + "]";
    }

    GlobalSymbol symbol = addDependency
        ? programValidationContext.resolve(name)  // Checks for cyclic dependencies.
        : program.getSymbol(name);

    // Refresh or invalidate transient symbols from previous runs.
    if (symbol != null && mode == ResolutionMode.LEGACY && symbol.scope == GlobalSymbol.Scope.TRANSIENT && symbol.stamp < program.currentStamp) {
      if (symbol.getType() == impliedType) {
        symbol.stamp = program.currentStamp;
      } else {
        symbol = null;
      }
    }

    if (symbol == null) {
      if (mode != ResolutionMode.PROGRAM && (forAssignment || mode == ResolutionMode.LEGACY)) {
        symbol = program.addTransientSymbol(name, impliedType, programValidationContext);
      } else {
        throw new RuntimeException("Variable not found: \"" + name + "\"");
      }
    } else {
      if (forAssignment && symbol.scope == GlobalSymbol.Scope.BUILTIN) {
        throw new RuntimeException("Can't overwrite builtin " + name);
      }
    }

    if (addDependency && symbol.initializer != null) {
      dependencies.add(symbol);
    }
    return symbol;
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
