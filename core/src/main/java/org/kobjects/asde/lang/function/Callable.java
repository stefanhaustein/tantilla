package org.kobjects.asde.lang.function;

import org.kobjects.asde.lang.runtime.EvaluationContext;

public  interface Callable {
  Object call(EvaluationContext evaluationContext, int paramCount);
}
