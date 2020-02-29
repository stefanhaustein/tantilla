package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.function.Callable;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.type.Type;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UserProperty implements Property {
  Classifier owner;
  String name;
  Map<Node, Exception> errors = Collections.emptyMap();
  Set<Property> dependencies;
  Type fixedType;
  Object staticValue;
  Node initializer;
  int fieldIndex = -1;
  boolean isInstanceField;
  boolean isMutable;

  public static Property createMethod(Classifier owner, String functionName, Callable methodImplementation) {
    return new UserProperty(
        owner,
        /* isInstanceField= */ false,
        /* isMutable= */ false,
        methodImplementation.getType(),
        functionName,
        /* initializer= */ null,
        methodImplementation);
  }

  public static Property createWithInitializer(Classifier owner, String name, Node initializer) {
    return new UserProperty(
        owner,
        /* isInstanceField= */ true,
        /* isMutable= */ true,
        /* fixedType= */ null,
        name,
        initializer,
        /* staticValue */ null);
  }

  UserProperty(Classifier owner, boolean isInstanceField, boolean isMutable, Type fixedType, String name, Node initializer, Object staticValue) {
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
    this.dependencies = dependencies;
    this.errors = errors;
  }


  @Override
  public String getName() {
    return name;
  }

  @Override
  public Type getType() {
    return initializer != null ? initializer.returnType() : fixedType;
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

  public void init(EvaluationContext evaluationContext, HashSet<UserProperty> initialized) {
    if (initialized.contains(this)) {
      return;
    }
    if (dependencies != null) {
      for (Property dep : dependencies) {
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
  public String toString() {
    return getName() + " -> " + getType();
  }

  @Override
  public Object get(EvaluationContext context, Object instance) {
    return isInstanceField ? ((Instance) instance).properties[fieldIndex] : staticValue;
  }

  @Override
  public void set(EvaluationContext context, Object instance, Object value) {
    ((Instance) instance).properties[fieldIndex] = value;
  }


}
