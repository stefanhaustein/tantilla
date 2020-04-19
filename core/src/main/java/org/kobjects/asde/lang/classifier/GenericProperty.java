package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.function.Callable;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.function.UserFunction;
import org.kobjects.asde.lang.node.EvaluationException;
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
  Set<Property> initializationDependencies = Collections.emptySet();
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
        /* fixedType= */ null,
        functionName,
        /* initializer= */ null,
        methodImplementation);
  }

  public static GenericProperty createWithInitializer(Classifier owner, boolean isInstanceField, boolean isMutable, String propertyName, Node initializer) {
    return new GenericProperty(
        owner,
        isInstanceField,
        isMutable,
        /* fixedType= */ null,
        propertyName,
        initializer,
        /* staticValue */ null);
  }

  public static GenericProperty createUninitialized(Classifier owner, boolean isMutable, String propertyName, Type type) {
    return new GenericProperty(
        owner,
        /* isInstanceField= */ true,
        isMutable,
        type,
        propertyName,
        /* initializer= */ null,
        /* staticValue= */ null);
  }

  public static GenericProperty createStatic(Classifier owner, String propertyName, Object value) {
    return new GenericProperty(
        owner,
        /* isInstanceField= */ false,
        /* isMutable */ false,
        /* fixedType */ null,
        propertyName,
        /* initializer= */ null,
        /* staticValue= */ value);
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
        // Safer than making sure all nodes don't throw when asking for an unresolved return value.
        // TODO: Might make sense to have a special type for this case instead of null.
        // e.printStackTrace();
      }
    }
    return Types.of(staticValue);
  }

  public int getFieldIndex() {
    return fieldIndex;
  }

  public Classifier getOwner() {
    return owner;
  }

  @Override
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
    return isMutable;
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
    initialized.add(this);
    if (initializationDependencies != null) {
      for (Property dep : initializationDependencies) {
        dep.init(evaluationContext, initialized);
      }
    }
    if (!isInstanceField && initializer != null) {
      staticValue = initializer.eval(evaluationContext);
    }
  }

  @Override
  public void changeFunctionType(FunctionType type) {
    ((UserFunction) staticValue).setType(type);
  }


  @Override
  public Object get(EvaluationContext context, Object instance) {
    if (instance instanceof AdapterInstance) {
      throw new RuntimeException("Instance is adapter instance in " + owner + "." + name);
    }
    return isInstanceField ? ((ClassInstance) instance).properties[fieldIndex] : staticValue;
  }

  @Override
  public void set(EvaluationContext context, Object instance, Object value) {
    if (!isMutable || !isInstanceField) {
      throw new IllegalStateException("Property " + this + (isInstanceField ? " is a static field!" : " is not mutable!"));
    }
    ((ClassInstance) instance).properties[fieldIndex] = value;
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

  @Override
  public Set<Property> getInitializationDependencies() {
    return initializationDependencies;
  }

  public void setFieldIndex(int i) {
    fieldIndex = i;
  }
}
