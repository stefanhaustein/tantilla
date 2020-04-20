package org.kobjects.asde.lang.classifier.module;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.classifier.AbstractClassifier;
import org.kobjects.asde.lang.classifier.StaticProperty;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.program.Program;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.TreeMap;

public class Module extends AbstractClassifier {
  TreeMap<String, StaticProperty> builtins = new TreeMap<>();

  public Module(Program program) {
    super(program);
  }

  public void addBuiltin(String name, Object value) {
    addBuiltin(StaticProperty.createWithStaticValue(this, name, value));
  }

  public void addBuiltin(StaticProperty property) {
    builtins.put(property.getName(), property);
  }

  public Collection<StaticProperty> builtins() {
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
  public void toString(AnnotatedStringBuilder asb, String indent, boolean includeContent, boolean forExport) {
    listProperties(asb, indent, includeContent, forExport);
  }
}
