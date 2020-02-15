package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.function.FunctionImplementation;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.statement.AbstractDeclarationStatement;
import org.kobjects.asde.lang.type.Type;


public class UserMethod extends AbstractUserClassProperty {
  FunctionImplementation methodImplementation;


  UserMethod(UserClass classImplementation, String name, FunctionImplementation methodImplementation) {
    super(classImplementation, name);
    this.methodImplementation = methodImplementation;
    methodImplementation.setDeclaringSymbol(this);
  }

  // May also be called from ClassValidationContext.
  void validate(ClassValidationContext classValidationContext) {
    if (classValidationContext.validated.contains(this)) {
      return;
    }

    FunctionValidationContext context = new FunctionValidationContext(classValidationContext, methodImplementation);

      methodImplementation.validate(context);

    if (context.errors.size() > 0) {
      System.err.println("Validation errors for property " + name + ": " + context.errors);
    }

    this.errors = context.errors;
    classValidationContext.errors.putAll(context.errors);
    classValidationContext.dependencies.addAll(context.dependencies);
    classValidationContext.validated.add(this);
  }



  @Override
  public Type getType() {
    return methodImplementation.getType();
  }

/*
  @Override
  public Object get(EvaluationContext evaluationContext) {
    return evaluationContext.self.getProperty(this).get();
  }

  @Override
  public void set(EvaluationContext evaluationContext, Object value) {
    evaluationContext.self.getProperty(this).set(value);
  }*/

  @Override
  public Object getValue() {
    return methodImplementation;
  }

  @Override
  public Node getInitializer() {
    return null;
  }

  @Override
  public void validate() {
    if (owner.declaringSymbol != null) {
      owner.declaringSymbol.validate();
    }
  }

  @Override
  public boolean isConstant() {
    return true;
  }

  @Override
  public void setName(String newName) {
    name = newName;
  }


  @Override
  public String toString() {
    return getName() + " -> " + getType();
  }
}
