package org.kobjects.asde.lang;

import org.kobjects.typesystem.PropertyDescriptor;
import org.kobjects.typesystem.Type;

public class MemberSymbol implements ResolvedSymbol {
  final PropertyDescriptor propertyDescriptor;

  public MemberSymbol(PropertyDescriptor descriptor) {
    this.propertyDescriptor = descriptor;
  }

  @Override
  public Object get(EvaluationContext evaluationContext) {
    return evaluationContext.self.getProperty(propertyDescriptor).get();
  }

  @Override
  public void set(EvaluationContext evaluationContext, Object value) {
    evaluationContext.self.getProperty(propertyDescriptor).set(value);
  }

  @Override
  public Type getType() {
    return propertyDescriptor.type();
  }

  @Override
  public boolean isConstant() {
    return false;
  }
}
