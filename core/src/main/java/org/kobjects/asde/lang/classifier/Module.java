package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.program.Program;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeMap;

public class Module extends ClassType {
  TreeMap<String, Object> builtins = new TreeMap<>();

  public Module(Program program) {
    super(program);
  }

  public void addBuiltin(String name, Object value) {
    builtins.put(name, value);
    putProperty(GenericProperty.createStatic(this, name, value));
  }

  public Map<String, Object> builtins() {
    return builtins;
  }


  @Override
  public Collection<GenericProperty> getUserProperties() {
    ArrayList<GenericProperty> userProperties = new ArrayList<>();
    for (Property property : propertyMap.values()) {
      if (property instanceof GenericProperty && !builtins.containsKey(property.getName())) {
        userProperties.add((GenericProperty) property);
      }
    }
    return userProperties;
  }

  public Collection<Property> getBuiltinProperties() {
    LinkedHashSet<Property> result = new LinkedHashSet<>();
    for (String key : builtins.keySet()) {
      result.add(getProperty(key));
    }
    return result;
  }
}
