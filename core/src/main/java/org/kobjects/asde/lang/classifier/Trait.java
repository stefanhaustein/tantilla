package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.type.MetaType;
import org.kobjects.asde.lang.type.Type;

import java.util.Collection;
import java.util.TreeMap;

public class Trait implements Classifier, DeclaredBy {
  Program program;
  Property declaringSymbol;
    public final TreeMap<String, TraitProperty> propertyMap = new TreeMap<>();

  public Trait(Program program) {
    this.program = program;
  }

  @Override
  public void setDeclaredBy(Property declaringSymbol) {
    this.declaringSymbol = declaringSymbol;
  }

  @Override
  public Property getDeclaringSymbol() {
    return declaringSymbol;
  }

  @Override
  public Property getProperty(String name) {
    return propertyMap.get(name);
  }

  @Override
  public Collection<? extends Property> getAllProperties() {
    return propertyMap.values();
  }

  @Override
  public CharSequence getDocumentation() {
    return null;
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
    return new MetaType(this);
  }

  public void putProperty(Property property) {
    if (!(property instanceof TraitProperty)) {
      throw new RuntimeException("TraitProperty required");
    }
    propertyMap.put(property.getName(), (TraitProperty) property);
  }

  public void addProperty(boolean mutable, String name, Type type) {
    propertyMap.put(name, new TraitProperty(this, mutable, name, type));
  }

  @Override
  public void remove(String propertyName) {
    propertyMap.remove(propertyName);
  }

  @Override
  public boolean isAssignableFrom(Type other) {
    if (other == this) {
      return true;
    }
    if (!(other instanceof Classifier)) {
      return false;
    }
    Classifier otherInterface = (Classifier) other;

    for (TraitProperty propertyDescriptor : propertyMap.values()) {
      Property otherDescriptor = otherInterface.getProperty(propertyDescriptor.getName());
      if (otherDescriptor == null) {
        System.out.println(toString() + " is not assignable from " + other + ": property '" + propertyDescriptor.getName() + " is missing");
        return false;
      }
      if (propertyDescriptor.getType() instanceof FunctionType && otherDescriptor.getType() instanceof FunctionType) {
        if (!((FunctionType) propertyDescriptor.getType()).isAssignableFrom((FunctionType) otherDescriptor.getType(), true)) {
          System.out.println(toString() + " is not assignable from " + other + ": expected type for property '" + propertyDescriptor.getName() + "': " + propertyDescriptor.getType() + " does not match " + otherDescriptor.getType());
          return false;
        }
      } else if (!propertyDescriptor.getType().equals(otherDescriptor.getType())) {
        System.out.println(toString() + " is not assignable from " + other + ": expected type for property '" + propertyDescriptor.getName() + "': " + propertyDescriptor.getType() + " does not match " + otherDescriptor.getType());
        return false;
      }
    }
    return true;

  }

  @Override
  public String toString() {
    return declaringSymbol != null ? declaringSymbol.getName() : super.toString();
  }

}
