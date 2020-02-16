package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.program.GlobalSymbol;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.statement.DeclarationStatement;
import org.kobjects.asde.lang.symbol.StaticSymbol;
import org.kobjects.asde.lang.function.FunctionImplementation;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.type.Type;

import java.util.Collections;
import java.util.Map;

public class UserClassProperty implements Property, StaticSymbol {

  static UserClassProperty createMethod(UserClass owner, String name, FunctionImplementation functionImplementation) {
    return new UserClassProperty(owner, name, functionImplementation);
  }

  static UserClassProperty createProperty(UserClass owner, String name, Node initializer) {
    return new UserClassProperty(owner, name, initializer);
  }

  static UserClassProperty createUninitializedProperty(UserClass owner, String name, Type type) {
    return new UserClassProperty(owner, name, type);
  }


  UserClass owner;
  String name;
  Map<Node, Exception> errors = Collections.emptyMap();
  Type fixedType;

  // Method
  FunctionImplementation methodImplementation;

  // Property
  Node initializer;
  int index = -1;
  boolean isInstanceField;


  private UserClassProperty(UserClass owner, String name, FunctionImplementation methodImplementation) {
      this.owner = owner;
      this.name = name;

      this.methodImplementation = methodImplementation;
      this.fixedType = methodImplementation.getType();

    methodImplementation.setDeclaringSymbol(this);

    this.isInstanceField = false;
  }

  private UserClassProperty(UserClass owner, String name, Node initializer) {
      this.owner = owner;
      this.name = name;

      this.initializer = initializer;
    this.isInstanceField = true;
  }

  private UserClassProperty(UserClass owner, String name, Type type) {
    this.owner = owner;
    this.name = name;
    this.fixedType = type;
    this.isInstanceField = true;
  }


  // May also be called from ClassValidationContext.
  void validate(ClassValidationContext classValidationContext) {
    if (classValidationContext.validated.contains(this)) {
      return;
    }

    FunctionValidationContext context = new FunctionValidationContext(classValidationContext, methodImplementation);

    if (methodImplementation != null) {
      methodImplementation.validate(context);
    } else {
      if (initializer != null) {
        initializer.resolve(context, 0);
      }
      index = owner.resolvedInitializers.size();
      owner.resolvedInitializers.add(initializer);
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

  public int getIndex() {
    return index;
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
  public Object getValue() {
    return methodImplementation;
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
    return methodImplementation == null;
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
    return methodImplementation == null ? ((Instance) instance).properties[index] : methodImplementation;
  }

  @Override
  public void set(EvaluationContext context, Object instance, Object value) {
    ((Instance) instance).properties[index] = value;
  }


}
