package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.runtime.EvaluationContext;

public interface InstantiableClassType extends Classifier {
  ClassInstance createInstance(EvaluationContext evaluationContext, Object... values);
  FunctionType getConstructorSignature();
}
