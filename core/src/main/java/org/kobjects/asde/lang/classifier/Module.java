package org.kobjects.asde.lang.classifier;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.program.Program;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeMap;

public class Module extends AbstractClassifier {
  TreeMap<String, GenericProperty> builtins = new TreeMap<>();

  public Module(Program program) {
    super(program);
  }

  public void addBuiltin(String name, Object value) {
    addBuiltin(GenericProperty.createStatic(this, name, value));
  }

  public void addBuiltin(GenericProperty property) {
    builtins.put(property.name, property);
  }

  public Collection<GenericProperty> builtins() {
    return builtins.values();
  }

  public Property getProperty(String name) {
    Property result = super.getProperty(name);
    return result == null ? builtins.get(name) : result;
  }

  public Collection<Property> getBuiltinProperties() {
    LinkedHashSet<Property> result = new LinkedHashSet<>();
    for (String key : builtins.keySet()) {
      result.add(getProperty(key));
    }
    return result;
  }

  @Override
  public CharSequence getDocumentation() {
    return null;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb) {
    asb.append(toString());
  }
}
