package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.symbol.Declaration;
import org.kobjects.asde.lang.symbol.StaticSymbol;
import org.kobjects.asde.lang.symbol.SymbolOwner;
import org.kobjects.asde.lang.type.MetaType;
import org.kobjects.asde.lang.type.Type;

import java.util.Collection;
import java.util.TreeMap;

public class Trait implements Classifier, Declaration, SymbolOwner {
  Program program;
  StaticSymbol declaringSymbol;
    public final TreeMap<String, TraitProperty> propertyMap = new TreeMap<>();

  public Trait(Program program) {
    this.program = program;
  }

  @Override
  public void setDeclaringSymbol(StaticSymbol declaringSymbol) {
    this.declaringSymbol = declaringSymbol;
  }

  @Override
  public StaticSymbol getSymbol(String name) {
    return propertyMap.get(name);
  }

  @Override
  public void removeSymbol(StaticSymbol symbol) {
    propertyMap.remove(symbol.getName());
  }

  @Override
  public void addSymbol(StaticSymbol symbol) {
    propertyMap.put(symbol.getName(), (TraitProperty) symbol);
  }

  @Override
  public Property getPropertyDescriptor(String name) {
    return propertyMap.get(name);
  }

  @Override
  public Collection<? extends Property> getPropertyDescriptors() {
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

  public void addProperty(String name, Type type) {
    propertyMap.put(name, new TraitProperty(this, name, type));
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
      Property otherDescriptor = otherInterface.getPropertyDescriptor(propertyDescriptor.getName());
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
