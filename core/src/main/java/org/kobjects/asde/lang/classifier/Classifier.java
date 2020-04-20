package org.kobjects.asde.lang.classifier;


import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Consumer;
import org.kobjects.asde.lang.function.UserFunction;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.type.Type;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * Seems to make sense to keep this as an interface as long as we don't know what to do about
 * generics (List).
 */
public interface Classifier extends Type {

  default void processNodes(Consumer<Node> action) {
    for (Property symbol : getProperties()) {
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

  default void listProperties(AnnotatedStringBuilder asb, String indent, boolean includeContent, boolean forExport) {
    for(Property property : getProperties()) {
      property.toString(asb, indent, includeContent, forExport);
      asb.append("\n");
    }
  }

  Property getProperty(String name);

  Collection<? extends Property> getProperties();

  void putProperty(Property property);

  CharSequence getDocumentation();

  void remove(String propertyName);

  default Set<String> getAllPropertyNames() {
    TreeSet<String> result = new TreeSet<>();
    for (Property property : getProperties()) {
      result.add(property.getName());
    }
    return result;
  }

  void toString(AnnotatedStringBuilder asb, String indent, boolean includeContent, boolean forExport);
}

