package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.typesystem.Instance;
import org.kobjects.typesystem.InstanceType;

import java.util.Map;

public interface InstantiableType extends InstanceType {
  Instance createInstance(EvaluationContext evaluationContext, Object... values);

}