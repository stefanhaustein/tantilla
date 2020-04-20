package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.type.MetaType;
import org.kobjects.asde.lang.type.Type;

import java.util.Collection;
import java.util.HashMap;
import java.util.TreeSet;

public abstract class AbstractClassifier implements Classifier {

  public final Program program;
  private final HashMap<String, Property> properties = new HashMap<>();

  protected AbstractClassifier(Program program) {
    this.program = program;
  }

  @Override
  public Property getProperty(String name) {
    return properties.get(name);
  }

  @Override
  public Collection<? extends Property> getProperties() {
    return new TreeSet<>(properties.values());
  }

  @Override
  public void putProperty(Property property) {
    properties.put(property.getName(), property);
    program.notifyProgramChanged();
  }


  @Override
  public void remove(String propertyName) {
    properties.remove(propertyName);
    program.notifyProgramChanged();
  }

  @Override
  public boolean hasDefaultValue() {
    return false;
  }

  @Override
  public Object getDefaultValue() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Type getType() {
    return new MetaType(this);
  }
}
