package org.kobjects.asde.lang.statement;

import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.typesystem.Type;

public class UninitializedField extends AbstractDeclarationStatement {

  private Type type;


  public UninitializedField(Type type, String name) {
    super(name);
    this.type = type;
  }

  @Override
  public Object evalValue(EvaluationContext evaluationContext) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Type getValueType() {
    return type;
  }

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, int line) {

  }
}
