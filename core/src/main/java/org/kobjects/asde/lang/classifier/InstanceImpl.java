package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.FunctionImplementation;
import org.kobjects.asde.lang.property.Property;
import org.kobjects.asde.lang.property.PropertyDescriptor;

public class InstanceImpl extends Instance {
  final Property[] properties;

  public InstanceImpl(ClassImplementation clazz, Property[] properties) {
    super(clazz);
    this.properties = properties;
  }

  @Override
  public ClassImplementation getType() {
    return (ClassImplementation) super.getType();
  }

  @Override
  public Property getProperty(PropertyDescriptor rawDescriptor) {
    ClassPropertyDescriptor descriptor = (rawDescriptor instanceof ClassPropertyDescriptor)
        ? ((ClassPropertyDescriptor) rawDescriptor)
        : getType().getPropertyDescriptor(rawDescriptor.name());
    int index = descriptor.getIndex();
    if (index != -1) {
      return properties[index];
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
