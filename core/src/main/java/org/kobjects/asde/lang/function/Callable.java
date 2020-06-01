package org.kobjects.asde.lang.function;

import org.kobjects.asde.lang.classifier.HasDeclaringPropertyReference;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Typed;

public  interface Callable extends Typed, HasDeclaringPropertyReference {
  Object call(EvaluationContext evaluationContext, int paramCount);

  @Override
  FunctionType getType();

  default int getLocalVariableCount() {
    return getType().getParameterCount();
  }

  CharSequence getDocumentation(); // { return null; }

  default void setType(FunctionType functionType) {
    throw new UnsupportedOperationException();
  }
}
