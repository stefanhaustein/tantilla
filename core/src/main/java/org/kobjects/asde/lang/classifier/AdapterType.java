package org.kobjects.asde.lang.classifier;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.type.Type;

import java.util.Collection;

public class AdapterType implements Classifier {
  Classifier classifier;
  Trait trait;


  @Override
  public Property getProperty(String name) {
    return null;
  }

  @Override
  public Collection<? extends Property> getAllProperties() {
    return null;
  }

  @Override
  public void putProperty(Property property) {

  }

  @Override
  public CharSequence getDocumentation() {
    return null;
  }

  @Override
  public void remove(String propertyName) {

  }

  @Override
  public void toString(AnnotatedStringBuilder asb) {

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
    return null;
  }
}
