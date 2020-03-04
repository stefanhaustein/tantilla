package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.function.Callable;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.type.Types;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GenericProperty implements Property {
  Classifier owner;
  String name;
  Map<Node, Exception> errors = Collections.emptyMap();
  Set<Property> initializationDependencies;
  Type fixedType;
  Object staticValue;
  Node initializer;
  int fieldIndex = -1;
  boolean isInstanceField;
  boolean isMutable;

  public static Property createMethod(Classifier owner, String functionName, Callable methodImplementation) {
    return new GenericProperty(
        owner,
        /* isInstanceField= */ false,
        /* isMutable= */ false,
        methodImplementation.getType(),
        functionName,
        /* initializer= */ null,
        methodImplementation);
  }

  public static Property createWithInitializer(Classifier owner, String name, Node initializer) {
    return new GenericProperty(
        owner,
        /* isInstanceField= */ true,
        /* isMutable= */ true,
        /* fixedType= */ null,
        name,
        initializer,
        /* staticValue */ null);
  }


  public static GenericProperty createWithInitializer(Classifier owner, boolean isInstanceField, boolean isMutable, String propertyName, Node initializer) {
    return new GenericProperty(
        owner,
        isInstanceField,
        isMutable,
        /* fixedType= */ null,
        propertyName,
        initializer,
        /* staticValue */ null);
  }

  public static GenericProperty createUninitialized(Classifier owner, String propertyName, Type type) {
    return new GenericProperty(
        owner,
        /* isInstanceField= */ true,
        /* isMutable */ true,
        type,
        propertyName,
        /* initializer= */ null,
        /* staticValue= */ null);
  }

  public static GenericProperty createStatic(Classifier owner, String propertyName, Object value) {
    return new GenericProperty(
        owner,
        /* isInstanceField= */ false,
        /* isMutable */ false,
        Types.of(value),
        propertyName,
        /* initializer= */ null,
        /* staticValue= */ value);
  }





  GenericProperty(Classifier owner, boolean isInstanceField, boolean isMutable, Type fixedType, String name, Node initializer, Object staticValue) {
    this.owner = owner;
    this.isInstanceField = isInstanceField;
    this.isMutable = isMutable;
    this.fixedType = fixedType;
    this.name = name;
    this.initializer = initializer;
    this.staticValue = staticValue;

      if (staticValue instanceof DeclaredBy) {
        ((DeclaredBy) staticValue).setDeclaredBy(this);
      }
  }

  /*
  // May also be called from ClassValidationContext.
  public void validate(ValidationContext parentValidationContext) {
    System.out.println("Validating user property: " + name);

    ValidationContext context = parentValidationContext.createChildContext(this);

    UserFunction userFunction =
        (!isInstanceField && initializer == null && staticValue instanceof UserFunction)
            ? (UserFunction) staticValue
            : null;
    if (userFunction != null) {
      System.out.println(" - " + name + " is userFunction");
      userFunction.validate(context);
    } else  {
      if (initializer != null) {
        System.out.println(" - " + name + " has initializer");
        initializer.resolve(context, 0);
      } else if (staticValue instanceof UserClass) {
        System.out.println(" - " + name + " is user class");
        ((UserClass) staticValue).validate(context);
      }
      if (isInstanceField) {
        System.out.println(" - " + name + " is instance field");
        fieldIndex = owner.resolvedInitializers.size();
        owner.resolvedInitializers.add(initializer);
      }
    }
    if (context.errors.size() > 0) {
      System.err.println("Validation errors for property " + name + ": " + context.errors);
    }

    this.errors = context.errors;
    this.dependencies = context.dependencies;
  //  parentValidationContext.dependencies.addAll(context.dependencies);
  //  parentValidationContext.errors.putAll(context.errors);
  }

   */
  @Override
  public void setDependenciesAndErrors(HashSet<Property> dependencies, HashMap<Node, Exception> errors) {
    this.initializationDependencies = dependencies;
    this.errors = errors;
  }


  @Override
  public String getName() {
    return name;
  }

  /**
   * May return null if the initializer is not resolved yet.
   */
  @Override
  public Type getType() {
    if (fixedType != null) {
      return fixedType;
    }
    if (initializer != null) {
      try {
        return initializer.returnType();
      } catch (Exception e) {
        // Safer than making sure all nodes don't throw when asking for an unresolved return value.
        // TODO: Might make sense to have a special type for this case instead of null.
        e.printStackTrace();
      }
    }
    return null;
  }

  public int getFieldIndex() {
    return fieldIndex;
  }

  public Classifier getOwner() {
    return owner;
  }

  public Map<Node, Exception> getErrors() {
    return errors;
  }

  @Override
  public Object getStaticValue() {
    return staticValue;
  }

  public Node getInitializer() {
    return initializer;
  }

  @Override
  public boolean isMutable() {
    return staticValue == null;
  }

  public  boolean isConstant() {
    return !isMutable();
  }

  @Override
  public boolean isInstanceField() {
    return isInstanceField;
  }

  public void setName(String newName) {
    name = newName;
  }

  public void init(EvaluationContext evaluationContext, HashSet<GenericProperty> initialized) {
    if (initialized.contains(this)) {
      return;
    }
    if (initializationDependencies != null) {
      for (Property dep : initializationDependencies) {
        dep.init(evaluationContext, initialized);
      }
    }
    if (!isInstanceField && initializer != null) {
      staticValue = initializer.eval(evaluationContext);
    }
    initialized.add(this);
  }

  public void setInitializer(Node initializer) {
    this.initializer = initializer;
  }

  @Override
  public Object get(EvaluationContext context, Object instance) {
    return isInstanceField ? ((Instance) instance).properties[fieldIndex] : staticValue;
  }

  @Override
  public void set(EvaluationContext context, Object instance, Object value) {
    ((Instance) instance).properties[fieldIndex] = value;
  }

  @Override
  public void setStaticValue(Object value) {
    if (!isMutable || isInstanceField) {
      throw new IllegalStateException("Property " + this + (isInstanceField ? " is an instance field (= not static)!" : " is not mutable!"));
    }
    this.staticValue = value;
  }

  @Override
  public String toString() {
    return Property.toString(this);
  }

}
