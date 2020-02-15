package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.FunctionImplementation;
import org.kobjects.asde.lang.property.Property;
import org.kobjects.asde.lang.property.PropertyDescriptor;

public class InstanceImpl extends Instance {
  final Property[] properties;

  public InstanceImpl(UserClass clazz, Property[] properties) {
    super(clazz);
    this.properties = properties;
  }

  @Override
  public UserClass getType() {
    return (UserClass) super.getType();
  }

  @Override
  public Property getProperty(PropertyDescriptor rawDescriptor) {
    UserClassProperty descriptor = (rawDescriptor instanceof UserClassProperty)
        ? ((UserClassProperty) rawDescriptor)
        : getType().getPropertyDescriptor(rawDescriptor.getName());
    if (descriptor.getIndex() != -1) {
      return properties[descriptor.getIndex()];
    }
    final FunctionImplementation methodImplementation = descriptor.methodImplementation;
    return new Method(methodImplementation.getType()) {
      @Override
      public Object call(EvaluationContext evaluationContext, int paramCount) {
        return methodImplementation.callImpl(new EvaluationContext(evaluationContext, methodImplementation, InstanceImpl.this));
      }
      @Override
      public int getLocalVariableCount() {
        return methodImplementation.getLocalVariableCount();
      }
    };
  }
}
