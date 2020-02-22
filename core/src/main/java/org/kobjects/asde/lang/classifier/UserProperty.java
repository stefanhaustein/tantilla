package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.program.GlobalSymbol;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.statement.DeclarationStatement;
import org.kobjects.asde.lang.symbol.Declaration;
import org.kobjects.asde.lang.symbol.StaticSymbol;
import org.kobjects.asde.lang.function.UserFunction;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.type.Type;

import java.util.Collections;
import java.util.Map;

public class UserProperty implements Property, StaticSymbol {
  UserClass owner;
  String name;
  Map<Node, Exception> errors = Collections.emptyMap();
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



  // May also be called from ClassValidationContext.
  void validate(ClassValidationContext classValidationContext) {
    if (classValidationContext.validated.contains(this)) {
      return;
    }

    UserFunction userFunction = staticValue instanceof UserFunction ? (UserFunction) staticValue : null;
    FunctionValidationContext context = new FunctionValidationContext(classValidationContext.programValidationContext, FunctionValidationContext.ResolutionMode.PROGRAM, userFunction);

    if (userFunction != null) {
      userFunction.validate(context);
    } else  {
      if (initializer != null) {
        initializer.resolve(context, 0);
      }
      if (isInstanceField) {
        fieldIndex = owner.resolvedInitializers.size();
        owner.resolvedInitializers.add(initializer);
      }
    }
    if (context.errors.size() > 0) {
      System.err.println("Validation errors for property " + name + ": " + context.errors);
    }

    this.errors = context.errors;
    classValidationContext.errors.putAll(context.errors);
    classValidationContext.dependencies.addAll(context.dependencies);
    classValidationContext.validated.add(this);
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
  public void validate() {
    if (owner.declaringSymbol != null) {
      owner.declaringSymbol.validate();
    }
  }

  @Override
  public GlobalSymbol.Scope getScope() {
    return GlobalSymbol.Scope.PERSISTENT;
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
