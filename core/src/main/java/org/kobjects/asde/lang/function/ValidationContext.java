package org.kobjects.asde.lang.function;


import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.classifier.Struct;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.statement.BlockStatement;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.type.Type;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ValidationContext {

  private static final boolean DEBUG = true;

  public static ValidationContext createRootContext(Program program) {
    return new ValidationContext(program, null, null, null);
  }

  public static ValidationContext createForFunction(UserFunction userFunction) {
    return new ValidationContext(userFunction.program, null, null, userFunction);
  }


  public HashMap<Node, Exception> errors = new HashMap<>();

  /** Will be null when validating symbols! */
  public final UserFunction userFunction;

  private Property property;

  private int localSymbolCount;
  private Block currentBlock;
  public HashSet<Property> initializationDependencies = new HashSet<>();
  private ValidationContext parentContext;
  public Program program;

  private Set<Property> resolved;

  /**
   * True when resoling initializations. Determines whether dependencies are collected in
   * initializationDependencies. The distinction is important to allow circular references
   * outside of static initializaiton (e.g. recursive functions or functions calling each other).
   */
  private boolean inPropertyInitialization;

  private ValidationContext(Program program, ValidationContext parentContext, Property property, UserFunction userFunction) {
    this.program = program;
    this.parentContext = parentContext;
    this.property = property;
    this.userFunction = userFunction;

    resolved = parentContext == null ? new HashSet<>() : parentContext.resolved;

    ValidationContext parent = parentContext;

    while (parent != null) {
      if (DEBUG) {
        System.out.print(". ");
      }
      if (property != null && parent.property == property) {
        throw new RuntimeException("Circular reference to " + property);
      }
      parent = parent.parentContext;
    }

    if (DEBUG) {
      System.out.print("Validating ");
      if (property != null) {
        System.out.println("Property " + property.getName());
      } else {
        System.out.println("(non-property)");
      }
    }

    startBlock(null);
    if (userFunction != null) {
      for (int i = 0; i < userFunction.parameterNames.length; i++) {
        currentBlock.localSymbols.put(userFunction.parameterNames[i], new LocalSymbol(localSymbolCount++, userFunction.getType().getParameterType(i), false));
      }
    }
  }

  public void validateProperty(Property property) {
    if (!resolved.contains(property)) {
      createChildContext(property).validate();
    }
  }

  private void validate() {
    if (property != null) {
      if (resolved.contains(property)) {
        return;
      }

      Node initializer = property.getInitializer();
      if (property.getInitializer() != null) {
        inPropertyInitialization = true;
        initializer.resolve(this, 0);
        inPropertyInitialization = false;
      }

      // Recursion is ok starting here
      resolved.add(property);

      if (!property.isInstanceField() && property.getInitializer() == null && property.getStaticValue() instanceof Classifier) {
        ((Classifier) property.getStaticValue()).validate(this);
      }
    }



    if (userFunction != null) {
      userFunction.validate(this);
    }

    if (property != null) {
      property.setDependenciesAndErrors(initializationDependencies, errors);
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


  public LocalSymbol declareLocalVariable(String name, Type type, boolean mutable) {
    if (currentBlock.localSymbols.containsKey(name)) {
      throw new RuntimeException("Local variable named '" + name + "' already exists");
    }
    LocalSymbol result = new LocalSymbol(localSymbolCount++, type, mutable);
    currentBlock.localSymbols.put(name, result);
    return result;
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

  private ValidationContext createChildContext(Property property) {
    UserFunction userFunction = (!property.isMutable()
        && !property.isInstanceField() && property.getStaticValue() instanceof UserFunction) ?
        (UserFunction) property.getStaticValue() : null;
    return new ValidationContext(this.program, this, property, userFunction);
  }

  public void validateAndAddDependency(Property symbol) {
    if (inPropertyInitialization) {
      initializationDependencies.add(symbol);
    }
    validateProperty(symbol);
  }
}
