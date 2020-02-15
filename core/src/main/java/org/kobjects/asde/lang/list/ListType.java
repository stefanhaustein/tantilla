package org.kobjects.asde.lang.list;

import org.kobjects.asde.lang.property.MethodDescriptor;
import org.kobjects.asde.lang.property.NativeReadonlyPropertyDescriptor;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.type.MetaType;
import org.kobjects.asde.lang.property.PropertyDescriptor;
import org.kobjects.asde.lang.type.Type;

import java.util.ArrayList;
import java.util.Collection;

public class ListType implements Classifier {

  public final Type elementType;

  public ListType(Type elementType) {
    if (elementType == null) {
      throw new RuntimeException("ElementType must not be null");
    }
    this.elementType = elementType;
  }

  public ListType(Type elementType, int dimensionality) {
    this.elementType = dimensionality == 1 ? elementType : new ListType(elementType, dimensionality - 1);
  }


  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ListType)) {
      return false;
    }
    return elementType.equals(((ListType) o).elementType);
  }

  @Override
  public String toString() {
    return "List[" + elementType + "]";
  }

  @Override
  public Type getType() {
    return new MetaType(this);
  }

  @Override
  public PropertyDescriptor getPropertyDescriptor(String name) {
    switch (name) {
      case "clear":
        return new MethodDescriptor("clear", "Remove all elements from the list.", Types.VOID, ListType.this) {
        @Override
        public Object call(EvaluationContext evaluationContext, int paramCount) {
          ListImpl list = (ListImpl) evaluationContext.getParameter(0);
          list.clear();
          return null;
        }
      };
      case "append":
        return new MethodDescriptor("append", "Appends an element to the list", Types.VOID, ListType.this, elementType) {
          @Override
          public Object call(EvaluationContext evaluationContext, int paramCount) {
            ListImpl list = (ListImpl) evaluationContext.getParameter(0);
            Object value = evaluationContext.getParameter(1);
            list.append(value);
            return null;
          }
        };
      case "remove":
        return new MethodDescriptor("remove", "Removes the first occurrence of the given object from the list", Types.VOID, ListType.this, elementType) {
          @Override
          public Object call(EvaluationContext evaluationContext, int paramCount) {
            ListImpl list = (ListImpl) evaluationContext.getParameter(0);
            Object value = evaluationContext.getParameter(1);
            list.remove(value);
            return null;
          }
        };
      case "size":
        return new NativeReadonlyPropertyDescriptor("size", "The size of the list.", Types.FLOAT) {
          @Override
          public Object get(EvaluationContext context, Object instance) {
            return (double) ((ListImpl) instance).length();
          }
        };
      default:
        throw new IllegalArgumentException("Unrecognized array property: '" + name + "'");
    }
  }

  @Override
  public Collection<? extends PropertyDescriptor> getPropertyDescriptors() {
    ArrayList<PropertyDescriptor> result = new ArrayList<>();

    result.add(getPropertyDescriptor("append"));
    result.add(getPropertyDescriptor("remove"));
    result.add(getPropertyDescriptor("size"));
    result.add(getPropertyDescriptor("clear"));
    return result;
  }

  @Override
  public CharSequence getDocumentation() {
    return null;
  }

  @Override
  public boolean hasDefaultValue() {
    return false;
  }

  @Override
  public Object getDefaultValue() {
    throw new UnsupportedOperationException();
  }

  public Type getElementType() {
    return elementType;
  }


  @Override
  public boolean supportsChangeListeners() {
    return true;
  }


  @Override
  public void addChangeListener(Object instance, Runnable changeListener) {
    ((ListImpl) instance).length.addListener(unused -> changeListener.run());
  }
}
