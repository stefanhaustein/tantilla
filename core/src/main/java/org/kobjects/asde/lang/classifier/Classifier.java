package org.kobjects.asde.lang.classifier;


import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Consumer;
import org.kobjects.asde.lang.function.UserFunction;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.type.Type;

import java.util.Collection;

/**
 * Seems to make sense to keep this as an interface as long as we don't know what to do about
 * generics (List).
 */
public interface Classifier extends Type {

  static void list(AnnotatedStringBuilder asb, Iterable<? extends Property> properties, String indent) {
    for (Property property : properties) {
      asb.append(indent);
      if (!property.toString(asb)) {
        asb.append(": [\u2026]");
      }
      asb.append("\n");
    }
  }


  Property getProperty(String name);

  Collection<? extends Property> getAllProperties();

  void putProperty(Property property);

  CharSequence getDocumentation();

  void remove(String propertyName);

  /*
  default void validate(ValidationContext validationContext) {
    for (Property property : getAllProperties()) {
      validationContext.validateProperty(property);
    }
  }

   */

  default void processNodes(Consumer<Node> action) {
    for (Property symbol : getAllProperties()) {
      if (!symbol.isInstanceField()) {
        if (symbol.getInitializer() != null) {
          symbol.getInitializer().process(action);
        }
        if (symbol.getStaticValue() instanceof Classifier) {
          ((Classifier) symbol.getStaticValue()).processNodes(action);
        }
        if (symbol.getStaticValue() instanceof UserFunction) {
          ((UserFunction) symbol.getStaticValue()).processNodes(action);
        }
      }
    }
  }

  void toString(AnnotatedStringBuilder asb);
}

