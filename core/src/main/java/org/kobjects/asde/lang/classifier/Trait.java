package org.kobjects.asde.lang.classifier;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.io.SyntaxColor;
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


  @Override
  public void remove(String propertyName) {
    propertyMap.remove(propertyName);
  }

  @Override
  public String toString() {
    return getDeclaringSymbol() != null ? getDeclaringSymbol().getName() : super.toString();
  }

  @Override
  public void toString(AnnotatedStringBuilder asb) {
    asb.append("trait", SyntaxColor.KEYWORD);
    if (declaringSymbol != null) {
      asb.append(' ');
      asb.append(declaringSymbol.getName());
    }
  }
}
