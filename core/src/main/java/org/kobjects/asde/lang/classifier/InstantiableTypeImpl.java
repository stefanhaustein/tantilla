package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.typesystem.Instance;
import org.kobjects.typesystem.InstanceTypeImpl;

public abstract class InstantiableTypeImpl extends InstanceTypeImpl implements InstantiableType {
  public InstantiableTypeImpl(String name, CharSequence documentation) {
    super(name, documentation);
  }

  @Override
  public abstract Instance createInstance(EvaluationContext evaluationContext);
}
