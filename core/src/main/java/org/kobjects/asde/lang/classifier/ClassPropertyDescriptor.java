package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.program.GlobalSymbol;
import org.kobjects.asde.lang.symbol.ResolvedSymbol;
import org.kobjects.asde.lang.symbol.StaticSymbol;
import org.kobjects.asde.lang.function.FunctionImplementation;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.statement.AbstractDeclarationStatement;
import org.kobjects.typesystem.PropertyDescriptor;
import org.kobjects.typesystem.Type;

import java.util.Collections;
import java.util.Map;

public class ClassPropertyDescriptor implements PropertyDescriptor, ResolvedSymbol, StaticSymbol {
  String name;
  AbstractDeclarationStatement initializer;
  FunctionImplementation methodImplementation;
  int index = -1;
  private Map<Node, Exception> errors = Collections.emptyMap();
  private ClassImplementation classImplementation;

  ClassPropertyDescriptor(ClassImplementation classImplementation, String name, AbstractDeclarationStatement initializer) {
    this.name = name;
    this.initializer = initializer;
    this.classImplementation = classImplementation;
  }

  ClassPropertyDescriptor(ClassImplementation classImplementation, String name, FunctionImplementation methodImplementation) {
    this.name = name;
    this.methodImplementation = methodImplementation;
    methodImplementation.setDeclaringSymbol(this);
    this.classImplementation = classImplementation;
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
      initializer.resolve(context, null, 0, 0);

      index = classImplementation.resolvedInitializers.size();
      classImplementation.resolvedInitializers.add(initializer);
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
  public String name() {
    return name;
  }

  @Override
  public Type type() {
    return methodImplementation != null ? methodImplementation.getType() : initializer.getValueType();
  }

  public int getIndex() {
    return index;
  }

  @Override
  public Object get(EvaluationContext evaluationContext) {
    return evaluationContext.self.getProperty(this).get();
  }

  @Override
  public void set(EvaluationContext evaluationContext, Object value) {
    evaluationContext.self.getProperty(this).set(value);
  }

  @Override
  public ClassImplementation getOwner() {
    return classImplementation;
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
  public String getName() {
    return name;
  }

  @Override
  public Type getType() {
    return type();
  }

  @Override
  public Node getInitializer() {
    return initializer;
  }

  @Override
  public void validate() {
    if (classImplementation.declaringSymbol != null) {
      classImplementation.declaringSymbol.validate();
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
}
