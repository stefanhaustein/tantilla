package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.runtime.EvaluationContext;

public interface InstantiableType extends Classifier {
  Instance createInstance(EvaluationContext evaluationContext, Object... values);

}
