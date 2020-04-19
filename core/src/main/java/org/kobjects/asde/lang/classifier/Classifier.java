package org.kobjects.asde.lang.classifier;


import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.type.Type;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * Seems to make sense to keep this as an interface as long as we don't know what to do about
 * generics (List).
 */
public interface Classifier extends Type {

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


  void toString(AnnotatedStringBuilder asb);
}

