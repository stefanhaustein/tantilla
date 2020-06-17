package org.kobjects.asde.lang.classifier.builtin;

import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.type.TypeImpl;

import java.util.Collection;
import java.util.TreeMap;

/**
 * Doesn't implement abstract classifer to avoid the program reference / notification.
 */
public class NativeClass extends TypeImpl implements Classifier {

  private final TreeMap<String, Property> propertyDescriptors = new TreeMap<>();
  private final CharSequence documentation;

  public NativeClass(String name, CharSequence documentation) {
    super(name, null);
    this.documentation = documentation;
  }

  /**
   * Properties are added separately to allow for circular references.
   */
  public void addProperties(Property... properties) {
    for (Property property : properties) {
      this.propertyDescriptors.put(property.getName(), property);
    }
  }

  public Property getProperty(String name) {
    return propertyDescriptors.get(name);
  }

  @Override
  public Collection<? extends Property> getProperties() {
    return propertyDescriptors.values();
  }

  @Override
  public void putProperty(Property property) {
    this.propertyDescriptors.put(property.getName(), property);
  }

  public CharSequence getDocumentation() {
    return documentation;
  }

  @Override
  public void remove(String propertyName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, String indent, boolean includeContent, boolean forExport) {
    asb.append("(native class)");
  }
}

