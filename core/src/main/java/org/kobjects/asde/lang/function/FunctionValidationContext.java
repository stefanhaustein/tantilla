package org.kobjects.asde.lang.function;


import org.kobjects.asde.lang.program.GlobalSymbol;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.program.ProgramValidationContext;
import org.kobjects.asde.lang.statement.BlockStatement;
import org.kobjects.asde.lang.symbol.ResolvedSymbol;
import org.kobjects.asde.lang.classifier.ClassValidationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.typesystem.Type;

import java.util.HashMap;
import java.util.HashSet;

public class FunctionValidationContext {
  public enum ResolutionMode {PROGRAM, INTERACTIVE};

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
    this.mode = mode;
    this.functionImplementation = functionImplementation;
    startBlock(null, 0);
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


  public void startBlock(BlockStatement startStatement, int line) {
    currentBlock = new Block(currentBlock, startStatement, line);
  }

  public BlockStatement endBlock(Node endStatement, int endLine) {
    BlockStatement startStatement = currentBlock.startStatement;
    startStatement.onResolveEnd(this, endStatement, endLine);
    currentBlock = currentBlock.parent;
    return startStatement;
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

    GlobalSymbol symbol = addDependency
        ? programValidationContext.resolve(name)  // Checks for cyclic dependencies.
        : program.getSymbol(name);

    if (symbol == null) {
      if (mode != ResolutionMode.PROGRAM && (forAssignment)) {
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

  public Block getCurrentBlock() {
    return currentBlock;
  }

}
