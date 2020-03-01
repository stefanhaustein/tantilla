package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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

  // Node getInitializer();

 // void validate(ValidationContext validationContext);

  default void setStaticValue(Object value) {
    throw new RuntimeException(toString(this) + " does not support setting a static value.");
  }

  default Node getInitializer() {
    return null;
  };

  default void setDependenciesAndErrors(HashSet<Property> dependencies, HashMap<Node, Exception> errors) {
    if (dependencies.size() > 0 || errors.size() > 0) {
      throw new UnsupportedOperationException("Dependencies or errors not expected for " + toString(this) + "; dependencies: " + dependencies + " errors: " + errors);
    }
  }

  default void init(EvaluationContext evaluationContext, HashSet<GenericProperty> initialized) {
  }

  default void setInitializer(Node node) {
    throw new RuntimeException(toString(this) + "' does not support initializers.");
  }

}
