package org.kobjects.asde.lang.classifier.trait;

import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.function.Callable;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;

public class TraitProperty implements Property {
  private final Trait owner;
  private String name;
  private FunctionType type;
  private final Callable value;

  TraitProperty(Trait owner, String name, FunctionType type) {
    if (type.getParameterCount() == 0 || !type.getParameter(0).getName().equals("self")) {
      throw new RuntimeException("All trait properties must be methods.");
    }

    this.owner = owner;
    this.name = name;
    this.type = type;
    this.value = new Callable() {
      @Override
      public Object call(EvaluationContext evaluationContext, int paramCount) {
        AdapterInstance adapterInstance = (AdapterInstance) evaluationContext.getParameter(0);

        evaluationContext.push(adapterInstance.instance);
        evaluationContext.popN(1);

        Callable callable = (Callable) adapterInstance.adapterType.getProperty(name).getStaticValue();

        evaluationContext.ensureExtraStackSpace(callable.getLocalVariableCount());

        return callable.call(evaluationContext, paramCount);
      }

      @Override
      public FunctionType getType() {
        return (FunctionType) TraitProperty.this.getType();
      }

      @Override
      public Property getDeclaringSymbol() {
        return TraitProperty.this;
      }
    };
  }

  public static Property create(Trait owner, String name, FunctionType type) {
    return new TraitProperty(owner, name, type);
  }

  @Override
  public Trait getOwner() {
    return owner;
  }


  @Override
  public Object getStaticValue() {
    return value;
  }

  @Override
  public boolean isMutable() {
    return false;
  }

  @Override
  public boolean isInstanceField() {
    return false;
  }

  @Override
  public void setName(String newName) {
    this.name = newName;
  }


  @Override
  public String getName() {
    return name;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public Object get(EvaluationContext context, Object instance) {
    return value;
  }

  @Override
  public void set(EvaluationContext context, Object instance, Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void changeFunctionType(FunctionType type) {
    this.type = type;
  }

}
