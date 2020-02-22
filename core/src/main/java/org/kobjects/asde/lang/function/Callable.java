package org.kobjects.asde.lang.function;

import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Typed;

public  interface Callable extends Typed {
  Object call(EvaluationContext evaluationContext, int paramCount);

  @Override
  FunctionType getType();

  default int getLocalVariableCount() {
    return getType().getParameterCount();
  }

  default CharSequence getDocumentation() { return null; }
}
