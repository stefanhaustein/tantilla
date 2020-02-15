package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.program.GlobalSymbol;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.symbol.StaticSymbol;
import org.kobjects.asde.lang.function.FunctionImplementation;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.statement.AbstractDeclarationStatement;
import org.kobjects.asde.lang.property.PropertyDescriptor;
import org.kobjects.asde.lang.type.Type;

import java.util.Collections;
import java.util.Map;

public class UserClassProperty implements PropertyDescriptor, StaticSymbol {

  UserClass owner;
  String name;
  Map<Node, Exception> errors = Collections.emptyMap();

  // Method
  FunctionImplementation methodImplementation;

  // Property
  AbstractDeclarationStatement initializer;
  int index = -1;


  UserClassProperty(UserClass owner, String name, FunctionImplementation methodImplementation) {
      this.owner = owner;
      this.name = name;

      this.methodImplementation = methodImplementation;
    methodImplementation.setDeclaringSymbol(this);
  }

  UserClassProperty(UserClass owner, String name, AbstractDeclarationStatement initializer) {
      this.owner = owner;
      this.name = name;

      this.initializer = initializer;
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
      initializer.resolve(context, 0);
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
    return methodImplementation == null ? initializer.getValueType() : methodImplementation.getType();
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
  public boolean isConstant() {
    return initializer == null;
  }

  @Override
  public void setName(String newName) {
    name = newName;
  }

  public void setInitializer(AbstractDeclarationStatement initializer) {
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
