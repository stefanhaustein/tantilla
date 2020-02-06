package org.kobjects.asde.lang.symbol;

import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;

public interface ResolvedSymbol {

  Object get(EvaluationContext evaluationContext);
  void set(EvaluationContext evaluationContext, Object value);
  Type getType();
  boolean isConstant();
}
