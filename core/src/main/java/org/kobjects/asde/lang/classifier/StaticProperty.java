package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.function.Callable;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.function.UserFunction;
import org.kobjects.asde.lang.expression.ExpressionNode;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.expression.Node;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.type.Types;

import java.util.HashSet;
import java.util.Map;

/**
 * Used for modules and classes
 */
public class StaticProperty extends AbstractProperty {
  Classifier owner;
  Object staticValue;

  public static Property createMethod(Classifier owner, String functionName, Callable methodImplementation) {
    return new StaticProperty(
        owner,
        /* isMutable= */ false,
        /* fixedType= */ null,
        functionName,
        /* initializer= */ null,
        methodImplementation);
  }

  public static StaticProperty createWithInitializer(Classifier owner, boolean isMutable, String propertyName, ExpressionNode initializer) {
    return new StaticProperty(
        owner,
        isMutable,
        /* fixedType= */ null,
        propertyName,
        initializer,
        /* staticValue */ null);
  }


  public static StaticProperty createWithStaticValue(Classifier owner, String propertyName, Object value) {
    return new StaticProperty(
        owner,
        /* isMutable */ false,
        /* fixedType */ null,
        propertyName,
        /* initializer= */ null,
        /* staticValue= */ value);
  }


  StaticProperty(Classifier owner, boolean isMutable, Type fixedType, String name, ExpressionNode initializer, Object staticValue) {
    super(isMutable, name, fixedType, initializer);
    this.owner = owner;
    this.staticValue = staticValue;
    if (staticValue instanceof DeclaredBy) {
      ((DeclaredBy) staticValue).setDeclaredBy(this);
    }
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
      return resolvedType;
    }
    return Types.of(staticValue);
  }

  @Override
  public Object get(EvaluationContext context, Object instance) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void set(EvaluationContext context, Object instance, Object value) {
    throw new UnsupportedOperationException();
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


  @Override
  public boolean isInstanceField() {
    return false;
  }

  public void setName(String newName) {
    name = newName;
  }

  public void init(EvaluationContext evaluationContext, HashSet<StaticProperty> initialized) {
    if (initialized.contains(this)) {
      return;
    }
    initialized.add(this);
    if (initializationDependencies != null) {
      for (Property dep : initializationDependencies) {
        dep.init(evaluationContext, initialized);
      }
    }
    if (resolvedInitializer != null) {
      staticValue = resolvedInitializer.run(evaluationContext).popObject();
    }
  }

  @Override
  public void changeFunctionType(FunctionType type) {
    ((UserFunction) staticValue).setType(type);
  }

  @Override
  public CharSequence getDocumentation() {
    // TODO
    return null;
  }


  @Override
  public void setStaticValue(Object value) {
    if (!mutable) {
      throw new IllegalStateException("Property " + this + " is not mutable!");
    }
    this.staticValue = value;
  }



}
