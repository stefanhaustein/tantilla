package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Collections;
import java.util.Map;

public abstract class NativeReadonlyProperty extends NativeProperty {

  public NativeReadonlyProperty(Classifier owner, String name, String description, Type type) {
    super(owner, name, description, type);
  }

  @Override
  public void set(EvaluationContext context, Object instance, Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isMutable() {
    return false;
  }

}
