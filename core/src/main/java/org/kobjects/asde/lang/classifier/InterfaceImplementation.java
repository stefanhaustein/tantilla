package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.symbol.Declaration;
import org.kobjects.asde.lang.symbol.StaticSymbol;
import org.kobjects.asde.lang.symbol.SymbolOwner;
import org.kobjects.typesystem.Instance;
import org.kobjects.typesystem.InstanceType;
import org.kobjects.typesystem.MetaType;
import org.kobjects.typesystem.PropertyDescriptor;
import org.kobjects.typesystem.Type;

import java.util.Collection;
import java.util.TreeMap;

public class InterfaceImplementation implements InstanceType, Declaration, SymbolOwner {
  Program program;
  StaticSymbol declaringSymbol;
    public final TreeMap<String, InterfacePropertyDescriptor> propertyMap = new TreeMap<>();

  public InterfaceImplementation(Program program) {
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
    propertyMap.put(symbol.getName(), (InterfacePropertyDescriptor) symbol);
  }

  @Override
  public PropertyDescriptor getPropertyDescriptor(String name) {
    return propertyMap.get(name);
  }

  @Override
  public Collection<? extends PropertyDescriptor> getPropertyDescriptors() {
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
    propertyMap.put(name, new InterfacePropertyDescriptor(this, name, type));
  }

  @Override
  public boolean isAssignableFrom(Type other) {
    if (other == this) {
      return true;
    }
    if (!(other instanceof InstanceType)) {
      return false;
    }
    InstanceType otherInterface = (InstanceType) other;

    for (InterfacePropertyDescriptor propertyDescriptor : propertyMap.values()) {
      PropertyDescriptor otherDescriptor = otherInterface.getPropertyDescriptor(propertyDescriptor.name());
      if (otherDescriptor == null) {
        System.out.println(toString() + " is not assignable from " + other + ": property '" + propertyDescriptor.name() + " is missing");
        return false;
      }
      if (!propertyDescriptor.type().equals(otherDescriptor.type())) {
        System.out.println(toString() + " is not assignable from " + other + ": expected type for property '" + propertyDescriptor.name() + "': " + propertyDescriptor.type() + " does not match " + otherDescriptor.type());
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
