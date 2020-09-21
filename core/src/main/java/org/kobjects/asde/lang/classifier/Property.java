package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.node.ExpressionNode;
import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.classifier.trait.Trait;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.function.UserFunction;
import org.kobjects.asde.lang.io.SyntaxColor;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.MetaType;
import org.kobjects.asde.lang.type.Type;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public interface Property extends Comparable<Property> {

  static String toString(Property property) {
    return property.getClass().getSimpleName() + " " + property.getOwner() + "." + property.getName() + " (type: " + property.getType() + "; initializer: " + property.getInitializer() + ")";
  }

  static int order(Property property) {
    if (property.getType() instanceof MetaType && ((MetaType) property.getType()).getWrapped() instanceof Classifier) {
      Classifier classifier = (Classifier) ((MetaType) property.getType()).getWrapped();
      if (classifier instanceof Trait) {
        return 4;
      }
      return 5;
    }
    if (property.isInstanceField()) {
      return property.isMutable() ? 7 : 8;
    }
    if (property.getType() instanceof FunctionType) {
      FunctionType functionType = (FunctionType) property.getType();
      return functionType.getParameterCount() > 0  && functionType.getParameter(0).getName().equals("self") ? 9 : property.getName().equals("main")? 10 : 3;
    }
    return property.isMutable() ? 2 : 1;
  }

  Classifier getOwner();

  default Map<Node, Exception> getErrors() {
    return Collections.emptyMap();
  }

  String getName();
  Type getType();

  Object get(EvaluationContext context, Object instance);

  void set(EvaluationContext context, Object instance, Object value);

  boolean isMutable();

  boolean isInstanceField();

  Object getStaticValue();

  default void setStaticValue(Object value) {
    throw new RuntimeException(toString(this) + " does not support setting a static value.");
  }

  default ExpressionNode getInitializer() {
    return null;
  };

  /**
   * Used to set validation results.
   */
  default void setDependenciesAndErrors(HashSet<Property> dependencies, HashMap<Node, Exception> errors) {
    if (dependencies.size() > 0 || errors.size() > 0) {
      throw new UnsupportedOperationException("Dependencies or errors not expected for " + toString(this) + "; dependencies: " + dependencies + " errors: " + errors);
    }
  }

  /** Called at program startup */
  default void init(EvaluationContext evaluationContext, HashSet<StaticProperty> initialized) {
  }

  default void setName(String newName) {
    throw new UnsupportedOperationException(toString(this) + "' does not support setName().");
  }

  default void toString(AnnotatedStringBuilder asb, String indent, boolean includeContent, boolean exportFormat) {
    if (includeContent && getStaticValue() instanceof UserFunction) {
      ((UserFunction) getStaticValue()).toString(asb, indent, exportFormat, getErrors());

    } else if (getType() instanceof FunctionType) {
      asb.append(indent);
      asb.append("def", SyntaxColor.KEYWORD);
      asb.append(' ');
      ((FunctionType) getType()).toString(asb, getName());
      asb.append(": […]");

    } else if (getType() instanceof MetaType && ((MetaType) getType()).getWrapped() instanceof Classifier) {
      Classifier classifier = (Classifier) ((MetaType) getType()).getWrapped();
      classifier.toString(asb, indent, includeContent, exportFormat);

    } else {
      asb.append(indent);
      asb.append(getName());
      if (getInitializer() != null) {
        asb.append(" = ");
        getInitializer().toString(asb, getErrors(), exportFormat);
      } else {
        asb.append(": ");
        asb.append(getType().toString());
      }
    }
  }

  default void changeFunctionType(FunctionType functionType) {
    throw new UnsupportedOperationException();
  }

  default Set<Property> getInitializationDependencies() {
    return Collections.emptySet();
  }

  default int compareTo(Property other) {
    int diff = Integer.compare(order(this), order(other));
    return diff == 0 ? getName().compareTo(other.getName()) : diff;
  }

  CharSequence getDocumentation();
}
