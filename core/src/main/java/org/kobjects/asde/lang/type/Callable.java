package org.kobjects.asde.lang.type;

import org.kobjects.asde.lang.EvaluationContext;

public  interface Callable {
  Object call(EvaluationContext evaluationContext, int paramCount);
}
