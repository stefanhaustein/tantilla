package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.statement.DeclarationStatement;
import org.kobjects.asde.lang.type.Type;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public interface Property {

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
    throw new UnsupportedOperationException();
  }

  default Node getInitializer() {
    return null;
  };

  default void setDependenciesAndErrors(HashSet<Property> dependencies, HashMap<Node, Exception> errors) {
    if (dependencies.size() > 0 || errors.size() > 0) {
      throw new UnsupportedOperationException("Dependencies or errors not expected for " + getName() + " of type " + getClass() + " dependencies: " + dependencies + " errors: " + errors);
    }
  }

  default void init(EvaluationContext evaluationContext, HashSet<UserProperty> initialized) {
  }

  default void setInitializer(Node node) {
    throw new RuntimeException("This property does not support initializers: " + getClass());
  }
}
