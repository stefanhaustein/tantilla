package org.kobjects.asde.lang.expression;

import org.kobjects.asde.lang.runtime.EvaluationContext;

public interface Assignable {
  void set(EvaluationContext evaluationContext, Object value);
}
