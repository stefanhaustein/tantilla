package org.kobjects.asde.lang.type;

import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.typesystem.Instance;
import org.kobjects.typesystem.InstanceType;

public interface InstantiableType extends InstanceType {
  Instance createInstance(EvaluationContext evaluationContext);
}
