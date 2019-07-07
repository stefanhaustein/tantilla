package org.kobjects.asde.lang.type;

import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.typesystem.Instance;
import org.kobjects.typesystem.InstanceTypeImpl;
import org.kobjects.typesystem.PropertyDescriptor;

public abstract class InstantiableTypeImpl extends InstanceTypeImpl implements InstantiableType {
  public InstantiableTypeImpl(String name, PropertyDescriptor... properties) {
    super(name, properties);
  }

  @Override
  public abstract Instance createInstance(EvaluationContext evaluationContext);
}
