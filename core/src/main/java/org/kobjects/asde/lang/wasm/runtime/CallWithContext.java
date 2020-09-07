package org.kobjects.asde.lang.wasm.runtime;

import org.kobjects.asde.lang.runtime.EvaluationContext;

public interface CallWithContext {
  void call(EvaluationContext context);
}
