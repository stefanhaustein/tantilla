package org.kobjects.typesystem;

import java.util.TreeMap;

public abstract class Classifier implements Type, Typed {

    TreeMap<String, PropertyDescriptor> propertyDescriptors = new TreeMap<>();

    public abstract Instance createInstance();

    public Classifier(PropertyDescriptor... properties) {
        for (PropertyDescriptor property : properties) {
            this.propertyDescriptors.put(property.name(), property);
        }
    }

    public PropertyDescriptor getPropertyDescriptor(String name) {
        return propertyDescriptors.get(name);
    }

    public Type getType() {
        return new MetaType(this);
    }


}
