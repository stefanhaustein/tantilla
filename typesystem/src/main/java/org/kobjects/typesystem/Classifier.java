package org.kobjects.typesystem;

import java.util.TreeMap;

public abstract class Classifier implements Type, Typed {

    TreeMap<String, PropertyDescriptor> properties = new TreeMap<>();
    public abstract Object createInstance();

    public Classifier(PropertyDescriptor... properties) {
        for (PropertyDescriptor property : properties) {
            this.properties.put(property.name(), property);
        }
    }

    public PropertyDescriptor getPropertyDescriptor(String name) {
        return properties.get(name);
    }

    public Type getType() {
        return new MetaType(this);
    }
}
