package org.kobjects.asde.lang.classifier;

import java.util.TreeMap;

public abstract class Classifier {

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
}
