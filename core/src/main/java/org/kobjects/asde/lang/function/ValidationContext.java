package org.kobjects.asde.lang.function;


import org.kobjects.asde.lang.classifier.DeclaredBy;
import org.kobjects.asde.lang.classifier.clazz.InstanceFieldProperty;
import org.kobjects.asde.lang.classifier.trait.AdapterType;
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

  public static boolean validateAll(Program program) {
    ValidationContext validationContext = new ValidationContext(program, null, null, null);
    validationContext.validateMembers(program.mainModule);
    return !validationContext.anyErrors;
  }

  public static ValidationContext validateShellInput(UserFunction userFunction) {
    ValidationContext result = new ValidationContext(userFunction.program, null, null, userFunction);
    result.validate();
    return result;
  }

  public static void reValidate(Program program, Property property) {
    new ValidationContext(program, null, property, null).validate();
  }

  // Shared across layers!
  private final HashMap<Property, ValidationContext> resolved;

  private final HashSet<Classifier> resolvedForInstantiation;

  public final Program program;
  /** Will be null when validating symbols! */
  public final UserFunction userFunction;
  public final Property property;

  public final HashMap<Node, Exception> errors = new HashMap<>();

  private int localSymbolCount;
  private Block currentBlock;
  private State state = State.UNINITIALIZED;
  private boolean anyErrors = false;


  public HashSet<Property> initializationDependencies = new HashSet<>();
  private ValidationContext parentContext;
  ArrayList<Runnable> whenDone = new ArrayList();


  private ValidationContext(Program program, ValidationContext parentContext, Property property, UserFunction userFunction) {
    this.program = program;
    this.parentContext = parentContext;
    this.property = property;
    this.userFunction = userFunction != null ? userFunction : property != null && (!property.isMutable()
        && !property.isInstanceField() && property.getStaticValue() instanceof UserFunction) ?
        (UserFunction) property.getStaticValue() : null;

    if (parentContext == null) {
      resolved = new HashMap<>();
      resolvedForInstantiation = new HashSet<>();
    } else {
      resolved = parentContext.resolved;
      resolvedForInstantiation = parentContext.resolvedForInstantiation;
    }

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

  public boolean validateProperty(Property property) {
    boolean ok = true;
    ValidationContext other = resolved.get(property);
    if (other == null) {
      other = createChildContext(property);
      other.validate();
    }
    Runnable addInitializationDependencies = () -> {
      if (!property.isInstanceField() && property.hasInitializer()) {
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
    return ok;
  }

  private void validate() {
    startBlock(null);

    if (userFunction != null) {
      FunctionType type = userFunction.type;
      for (int i = 0; i < type.getParameterCount(); i++) {
        type.getParameter(i).resolve(this);
        currentBlock.localSymbols.put(type.getParameter(i).getName(), new LocalSymbol(localSymbolCount++, type.getParameterType(i), false));
      }
    }

    if (property != null) {
      if (property.hasInitializer()) {
        property.resolveInitializer(this);
      }

      // Recursion should be ok from here on
      resolved.put(property, this);
    }

    state = State.SIGNATURE_RESOLVED;

    if (userFunction != null) {
      userFunction.validate(this);
    }

    if (property != null) {
      if (property.getType() instanceof MetaType && property.getStaticValue() instanceof AdapterType) {
        AdapterType adapterType = ((AdapterType) property.getStaticValue());
        Set<String> missingMethodNames = adapterType.trait.getAllPropertyNames();
        missingMethodNames.removeAll(adapterType.getAllPropertyNames());
        if (!missingMethodNames.isEmpty()) {
          addError(Node.NO_NODE, new RuntimeException("Missing trait methods: " + missingMethodNames));
        }
      }
      if (property.getOwner() instanceof AdapterType) {
        AdapterType adapterType = (AdapterType) property.getOwner();
        Property traitProperty = adapterType.trait.getProperty(property.getName());
        if (traitProperty == null) {
          addError(Node.NO_NODE, new RuntimeException("Not a Trait property."));
        } else if (!((FunctionType) property.getType()).equals((FunctionType) traitProperty.getType(), true)) {
          addError(Node.NO_NODE, new RuntimeException("Signature does not match trait property signature: " + traitProperty.getType()));
        }
      }

      property.setDependenciesAndErrors(initializationDependencies, errors);
    }

    // Hacky error bubbling...
    if (property != null) {
      Property p = property;
      while (p.getOwner() instanceof DeclaredBy) {
        Property owner = ((DeclaredBy) p.getOwner()).getDeclaringSymbol();
        if (owner == null) {
          break;
        }
        validateProperty(owner);
        if (errors.size() > 0) {
          owner.getErrors().put(Node.NO_NODE, new RuntimeException("Error(s) in '" + p.getName() + "'"));
        }
        p = owner;
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
    ValidationContext context = this;
    while(context != null && !context.anyErrors) {
      context.anyErrors = true;
      context = context.parentContext;
    }
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
    for (Property property : classifier.getProperties()) {
      validateProperty(property);
      if (!property.isInstanceField() && property.getStaticValue() instanceof Classifier) {
        validateMembers((Classifier) (property.getStaticValue()));
      }
    }
  }


  private void validateAllMethods(Classifier classifier) {
    for (Property property : classifier.getProperties()) {
      if (!property.hasInitializer() && property.getType() instanceof FunctionType) {

        FunctionType functionType = (FunctionType) property.getType();
        if (functionType.getParameterCount() > 0 && functionType.getParameter(0).getName().equals("self")) {
          validateProperty(property);
        }
      }
    }
  }

  public boolean hasAnyErrors() {
    return anyErrors;
  }


  public void addInstanceDependency(Classifier classifier) {
    if (resolvedForInstantiation.contains(classifier)) {
      return;
    }
    resolvedForInstantiation.add(classifier);
    int fieldIndex = 0;
    for (Property property : classifier.getProperties()) {
      if (property.isInstanceField()) {
        validateProperty(property);
        ((InstanceFieldProperty) property).setFieldIndex(fieldIndex++);
      }
    }
    // We need to initialize all methods, too -- as they can be called via traits.
    validateAllMethods(classifier);
  }
}
