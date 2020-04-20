package org.kobjects.asde.lang.classifier.clazz;

import org.kobjects.asde.lang.classifier.AbstractProperty;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;


public class InstanceFieldProperty extends AbstractProperty {
  ClassType owner;
  int fieldIndex = -1;


  public static InstanceFieldProperty createWithInitializer(ClassType owner, boolean isMutable, String propertyName, Node initializer) {
    return new InstanceFieldProperty(
        owner,
        isMutable,
        /* fixedType */ null,
        propertyName,
        initializer);
  }

  public static InstanceFieldProperty createUninitialized(ClassType owner, boolean isMutable, String propertyName, Type type) {
    return new InstanceFieldProperty(
        owner,
        isMutable,
        type,
        propertyName,
        /* initializer= */ null);
  }


  public InstanceFieldProperty(ClassType owner, boolean isMutable, Type type, String propertyName, Node initializer) {
    super(isMutable, propertyName, type, initializer);
    this.owner = owner;
  }


  @Override
  public ClassType getOwner() {
    return owner;
  }

  /**
   * May return null if the initializer is not resolved yet.
   */
  @Override
  public Type getType() {
    if (initializer != null) {
      try {
        return initializer.returnType();
      } catch (Exception e) {
        // Safer than making sure all nodes don't throw when asking for an unresolved return value.
        // TODO: Might make sense to have a special type for this case instead of null.
        // e.printStackTrace();
      }
    }
    return fixedType;
  }

  public void setFieldIndex(int fieldIndex) {
    this.fieldIndex = fieldIndex;
  }

  @Override
  public Object get(EvaluationContext context, Object instance) {
    return ((ClassInstance) instance).properties[fieldIndex];
  }

  @Override
  public void set(EvaluationContext context, Object instance, Object value) {
    ((ClassInstance) instance).properties[fieldIndex] = value;
  }

  @Override
  public boolean isMutable() {
    return mutable;
  }

  @Override
  public boolean isInstanceField() {
    return true;
  }

  @Override
  public Object getStaticValue() {
    // TODO: throw unsupported?
    return null;
  }


}
