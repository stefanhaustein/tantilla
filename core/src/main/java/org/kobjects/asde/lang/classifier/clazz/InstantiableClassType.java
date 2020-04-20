package org.kobjects.asde.lang.classifier.clazz;

import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.runtime.EvaluationContext;

public interface InstantiableClassType extends Classifier {
  ClassInstance createInstance(EvaluationContext evaluationContext, Object... values);
  FunctionType getConstructorSignature();
}
