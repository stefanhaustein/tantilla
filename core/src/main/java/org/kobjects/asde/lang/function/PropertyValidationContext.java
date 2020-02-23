package org.kobjects.asde.lang.function;


import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.classifier.UserProperty;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.program.ProgramValidationContext;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.statement.BlockStatement;
import org.kobjects.asde.lang.symbol.ResolvedSymbol;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.type.Type;

import java.util.HashMap;
import java.util.HashSet;

public class PropertyValidationContext {
  public enum ResolutionMode {PROGRAM, INTERACTIVE};

  public final Program program;
  public HashMap<Node, Exception> errors = new HashMap<>();
  public final ResolutionMode mode;

  /** Will be null when validating symbols! */
  public final UserFunction userFunction;

  private Property symbol;

  private int localSymbolCount;
  private Block currentBlock;
  public HashSet<UserProperty> dependencies = new HashSet<>();
  public final ProgramValidationContext programValidationContext;

  public PropertyValidationContext(ProgramValidationContext programValidationContext, ResolutionMode mode, Property symbol, UserFunction userFunction) {
    this.programValidationContext = programValidationContext;
    this.program = programValidationContext.program;
    this.mode = mode;
    this.symbol = symbol;
    this.userFunction = userFunction;
    startBlock(null);
    if (userFunction != null) {
      for (int i = 0; i < userFunction.parameterNames.length; i++) {
        currentBlock.localSymbols.put(userFunction.parameterNames[i], new LocalSymbol(localSymbolCount++, userFunction.getType().getParameterType(i), false));
      }
    }
  }

  public void startBlock(BlockStatement startStatement) {
    currentBlock = new Block(currentBlock, startStatement);
  }

  public BlockStatement endBlock() {
    BlockStatement startStatement = currentBlock.startStatement;
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

    ResolvedSymbol resolved = resolveVariableAccess(name, addDependency, true);
    // Doesn't check for assigning to constants here because this is also called from
    // resolveVariableDeclaration handling the initial assignment, so that part is checked in
    // AssignStatement.


    if (resolved.getType() != null && !resolved.getType().isAssignableFrom(type)) {
      throw new RuntimeException("Cannot assign value of type " + type + " to " + name + " of type " + resolved.getType());
    }

    return resolved;
  }

  public ResolvedSymbol resolveVariableAccess(String name) {
    return resolveVariableAccess(name, true, false);
  }

  private ResolvedSymbol resolveVariableAccess(String name, boolean addDependency, boolean forAssignment) {

    // Block level

    // System.out.println("resolveVariableAccess " + (classValidationContext == null ? "" : (classValidationContext.classImplementation + ".")) + name);

    ResolvedSymbol resolved = currentBlock.get(name);
    if (resolved != null) {
      return resolved;
    }

    // Members


    Property property = program.mainModule.getPropertyDescriptor(name);
    if (property != null) {
      return new ResolvedSymbol() {
        @Override
        public Object get(EvaluationContext evaluationContext) {
          return property.get(evaluationContext, null);
        }

        @Override
        public void set(EvaluationContext evaluationContext, Object value) {
          property.set(evaluationContext, null, value);
        }

        @Override
        public Type getType() {
          return property.getType();
        }

        @Override
        public boolean isConstant() {
          return !property.isMutable();
        }
      };
    }

    /*
    if (classValidationContext != null) {
      resolved = classValidationContext.resolve(name);
      if (resolved != null) {
        return resolved;
      }
    }
     */

    return null;
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
