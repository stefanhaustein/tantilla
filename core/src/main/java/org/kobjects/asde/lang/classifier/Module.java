package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.program.Program;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Module extends Struct {
  HashMap<String, Object> builtins = new HashMap<>();

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
}
