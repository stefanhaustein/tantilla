package org.kobjects.asde.lang.function;


import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.statement.BlockStatement;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.type.MetaType;
import org.kobjects.asde.lang.type.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ValidationContext {
  private static final boolean DEBUG = true;

  enum State {
    UNINITIALIZED,
    SIGNATURE_RESOLVED,
    FULLY_RESOLVED;
  }

  public static void validateAll(Program program) {
    new ValidationContext(program, null, null, null).validateMembers(program.mainModule);
  }

  public static ValidationContext validateShellInput(UserFunction userFunction) {
    ValidationContext result = new ValidationContext(userFunction.program, null, null, userFunction);
    result.validate();
    return result;
  }

  public static void reValidate(Program program, Property property) {
    new ValidationContext(program, null, property, null).validate();
  }

  public HashMap<Node, Exception> errors = new HashMap<>();

  /** Will be null when validating symbols! */
  public final UserFunction userFunction;

  private Property property;

  private int localSymbolCount;
  private Block currentBlock;
  private State state = State.UNINITIALIZED;

  /**
   * Properties with
   */
  public HashSet<Property> initializationDependencies = new HashSet<>();
  private ValidationContext parentContext;
  public Program program;

  private HashMap<Property, ValidationContext> resolved;
  ArrayList<Runnable> whenDone = new ArrayList();

  private ValidationContext(Program program, ValidationContext parentContext, Property property, UserFunction userFunction) {
    this.program = program;
    this.parentContext = parentContext;
    this.property = property;
    this.userFunction = userFunction != null ? userFunction : property != null && (!property.isMutable()
        && !property.isInstanceField() && property.getStaticValue() instanceof UserFunction) ?
        (UserFunction) property.getStaticValue() : null;

    resolved = parentContext == null ? new HashMap<>() : parentContext.resolved;

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
  }

  public void validateProperty(Property property) {
    ValidationContext other = resolved.get(property);
    if (other == null) {
      other = createChildContext(property);
      other.validate();
    }
    Runnable addInitializationDependencies = () -> {
      if (!property.isInstanceField() && property.getInitializer() != null) {
        initializationDependencies.add(property);
      } else {
        initializationDependencies.addAll(property.getInitializationDependencies());
      }
    };
    if (other.state == State.FULLY_RESOLVED) {
      addInitializationDependencies.run();
    } else {
      other.whenDone.add(addInitializationDependencies);
    }
  }

  private void validate() {
    startBlock(null);

    if (userFunction != null) {
      FunctionType type = userFunction.type;
      for (int i = 0; i < type.getParameterCount(); i++) {
        if (type.getParameter(i).getDefaultValueExpression() != null) {
          type.getParameter(i).getDefaultValueExpression().resolve(this, 0);
        }
        currentBlock.localSymbols.put(type.getParameter(i).getName(), new LocalSymbol(localSymbolCount++, type.getParameterType(i), false));
      }
    }

    if (property != null) {
      if (property.getInitializer() != null) {
        property.getInitializer().resolve(this, 0);
      }

      // Recursion should be ok from here on
      resolved.put(property, this);
    }

    state = State.SIGNATURE_RESOLVED;

    if (userFunction != null) {
      userFunction.validate(this);
    }

    if (property != null) {
      property.setDependenciesAndErrors(initializationDependencies, errors);
    }

    if (errors.size() != 0) {
      System.out.println("Errors for property " + property + ":  ");
      for (Throwable throwable : errors.values()) {
        throwable.printStackTrace(System.out);
      }
    }

    state = State.FULLY_RESOLVED;

    for (Runnable runnable : whenDone) {
      runnable.run();
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
    return new ValidationContext(this.program, this, property, null);
  }



  private void validateMembers(Classifier classifier) {
    for (Property property : classifier.getAllProperties()) {
      validateProperty(property);
      if (!property.isInstanceField() && property.getStaticValue() instanceof Classifier) {
        validateMembers((Classifier) (property.getStaticValue()));
      }
    }
  }


  class ResolutionState {

  }


}
