package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.statement.DeclarationStatement;
import org.kobjects.asde.lang.symbol.Declaration;
import org.kobjects.asde.lang.symbol.StaticSymbol;
import org.kobjects.asde.lang.function.UserFunction;
import org.kobjects.asde.lang.function.PropertyValidationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.type.Type;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UserProperty implements Property, StaticSymbol {
  UserClass owner;
  String name;
  Map<Node, Exception> errors = Collections.emptyMap();
  Set<StaticSymbol> dependencies;
  Type fixedType;
  Object staticValue;
  Node initializer;
  int fieldIndex = -1;
  boolean isInstanceField;
  boolean isMutable;


  UserProperty(UserClass owner, boolean isInstanceField, boolean isMutable, Type fixedType, String name, Node initializer, Object staticValue) {
    this.owner = owner;
    this.isInstanceField = isInstanceField;
    this.isMutable = isMutable;
    this.fixedType = fixedType;
    this.name = name;
    this.initializer = initializer;
    this.staticValue = staticValue;

      if (staticValue instanceof Declaration) {
        ((Declaration) staticValue).setDeclaringSymbol(this);
      }
  }


  @Override
  public void validate() {
    if (owner.declaringSymbol != null) {
      owner.declaringSymbol.validate();
    }
  }

  // May also be called from ClassValidationContext.
  public void validate(PropertyValidationContext classValidationContext) {
    System.out.println("Validating user property: " + name);

    UserFunction userFunction =
        (!isInstanceField && initializer == null && staticValue instanceof UserFunction)
        ? (UserFunction) staticValue
            : null;
    PropertyValidationContext context = new PropertyValidationContext(classValidationContext.programValidationContext, PropertyValidationContext.ResolutionMode.PROGRAM, this, userFunction);

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
    classValidationContext.dependencies.addAll(context.dependencies);
    classValidationContext.errors.putAll(context.errors);
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

  @Override
  public UserClass getOwner() {
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
  public Node getInitializer() {
    return initializer;
  }

  @Override
  public boolean isMutable() {
    return staticValue == null;
  }

  @Override
  public  boolean isConstant() {
    return !isMutable();
  }

  @Override
  public boolean isInstanceField() {
    return isInstanceField;
  }

  @Override
  public void setName(String newName) {
    name = newName;
  }

  @Override
  public void init(EvaluationContext evaluationContext, HashSet<StaticSymbol> initialized) {
    if (initialized.contains(this)) {
      return;
    }
    if (dependencies != null) {
      for (StaticSymbol dep : dependencies) {
        dep.init(evaluationContext, initialized);
      }
    }
    if (!isInstanceField && initializer != null) {
      staticValue = initializer.eval(evaluationContext);
    }
    initialized.add(this);
  }

  public void setInitializer(DeclarationStatement initializer) {
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
