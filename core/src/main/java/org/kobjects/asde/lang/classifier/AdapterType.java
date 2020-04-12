package org.kobjects.asde.lang.classifier;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.io.SyntaxColor;
import org.kobjects.asde.lang.type.MetaType;
import org.kobjects.asde.lang.type.Type;

import java.util.Collection;
import java.util.TreeMap;

public class AdapterType implements Classifier {
  public final Classifier classifier;
  public final Trait trait;
  TreeMap<String, Property> properties = new TreeMap<>();

  public AdapterType(Classifier classifier, Trait trait) {
    this.classifier = classifier;
    this.trait = trait;
  }


  @Override
  public Property getProperty(String name) {
    return properties.get(name);
  }

  @Override
  public Collection<? extends Property> getAllProperties() {
    return properties.values();
  }

  @Override
  public void putProperty(Property property) {
    properties.put(property.getName(), property);
  }

  @Override
  public CharSequence getDocumentation() {
    return null;
  }

  @Override
  public void remove(String propertyName) {
    properties.remove(propertyName);
  }

  @Override
  public void toString(AnnotatedStringBuilder asb) {
    asb.append("impl ");
    asb.append(trait.toString());
    asb.append(" for ");
    asb.append(classifier.toString());
  }

  @Override
  public boolean hasDefaultValue() {
    return false;
  }

  @Override
  public Object getDefaultValue() {
    return null;
  }

  @Override
  public Type getType() {
    return new MetaType(this);
  }

  public String toString() {
    return trait + " for " + classifier;
  }
}
