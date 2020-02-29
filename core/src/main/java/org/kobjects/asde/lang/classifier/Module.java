package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.program.Program;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Module extends UserClass {
  HashMap<String, Object> builtins = new HashMap<>();

  public Module(Program program) {
    super(program);
  }

  public void addBuiltin(String name, Object value) {
    builtins.put(name, value);
    putProperty(UserProperty.createStatic(this, name, value));
  }

  public Map<String, Object> builtins() {
    return builtins;
  }


  @Override
  public Collection<UserProperty> getUserProperties() {
    ArrayList<UserProperty> userProperties = new ArrayList<>();
    for (Property property : propertyMap.values()) {
      if (property instanceof UserProperty && !builtins.containsKey(property.getName())) {
        userProperties.add((UserProperty) property);
      }
    }
    return userProperties;
  }
}
