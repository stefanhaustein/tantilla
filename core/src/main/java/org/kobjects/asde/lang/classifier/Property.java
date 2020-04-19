package org.kobjects.asde.lang.classifier;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
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

public interface Property {

  static String toString(Property property) {
    return property.getClass().getSimpleName() + " " + property.getOwner() + "." + property.getName() + " (type: " + property.getType() + ")";
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

  default Node getInitializer() {
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
  default void init(EvaluationContext evaluationContext, HashSet<GenericProperty> initialized) {
  }

  default void setName(String newName) {
    throw new UnsupportedOperationException(toString(this) + "' does not support setName().");
  }

  default boolean toString(AnnotatedStringBuilder asb) {
    if (getType() instanceof FunctionType) {
      asb.append("def", SyntaxColor.KEYWORD);
      asb.append(' ');
      asb.append(getName());
      ((FunctionType) getType()).toString(asb);
      return false;
    }
    if (getType() instanceof MetaType && ((MetaType) getType()).getWrapped() instanceof Classifier) {
      Classifier classifier = (Classifier) ((MetaType) getType()).getWrapped();
      classifier.toString(asb);
      return false;
    }

    asb.append("(tbd) ");
    asb.append(getName());
    return true;
  }

  default void list(AnnotatedStringBuilder asb) {
    if (getType() instanceof FunctionType) {
      ((UserFunction) getStaticValue()).toString(asb, getErrors());
    } else if (getType() instanceof MetaType && ((MetaType) getType()).getWrapped() instanceof Classifier) {
      Classifier classifier = (Classifier) ((MetaType) getType()).getWrapped();
      classifier.toString(asb);
      asb.append(":\n");
      Classifiers.list(asb, classifier.getProperties(), " ");
      asb.append("end\n");
    } else {
      toString(asb);
    }
  }

  default void changeFunctionType(FunctionType functionType) {
    throw new UnsupportedOperationException();
  }

  default Set<Property> getInitializationDependencies() {
    return Collections.emptySet();
  }

}
