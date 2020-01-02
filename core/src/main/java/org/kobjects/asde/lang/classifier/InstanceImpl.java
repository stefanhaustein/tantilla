package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.FunctionImplementation;
import org.kobjects.typesystem.Instance;
import org.kobjects.typesystem.Property;
import org.kobjects.typesystem.PropertyDescriptor;

public class InstanceImpl extends Instance {
  final Property[] properties;

  public InstanceImpl(ClassImplementation clazz, Property[] properties) {
    super(clazz);
    this.properties = properties;
  }

  @Override
  public Property getProperty(PropertyDescriptor rawDescriptor) {
    ClassPropertyDescriptor descriptor = ((ClassPropertyDescriptor) rawDescriptor);
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
