package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.runtime.EvaluationContext;

public interface InstantiableType extends InstanceType {
  Instance createInstance(EvaluationContext evaluationContext, Object... values);

}
