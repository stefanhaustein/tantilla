package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.type.MetaType;
import org.kobjects.asde.lang.type.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

public class Struct implements Classifier, InstantiableType, DeclaredBy {

  final Program program;
  public final TreeMap<String, Property> propertyMap = new TreeMap<>();
  ArrayList<Node> resolvedInitializers = new ArrayList<>();
  Property declaringSymbol;

  public Struct(Program program) {
    this.program = program;
  }

  public Collection<GenericProperty> getUserProperties() {
    ArrayList<GenericProperty> userProperties = new ArrayList<>();
    for (Property property : propertyMap.values()) {
      if (property instanceof GenericProperty) {
        userProperties.add((GenericProperty) property);
      }
    }
    return userProperties;
  }

  @Override
  public Property getProperty(String name) {
    return propertyMap.get(name);
  }

  public GenericProperty getUserProperty(String name) {
    return (GenericProperty) propertyMap.get(name);
  }

  @Override
  public Collection<? extends Property> getAllProperties() {
    return propertyMap.values();
  }

  @Override
  public void putProperty(Property property) {
    propertyMap.put(property.getName(), property);
  }

  @Override
  public CharSequence getDocumentation() {
    return null;
  }

  @Override
  public Type getType() {
    return new MetaType(this);
  }


  public void validate(ValidationContext classValidationContext) {
    System.out.println("Userclass validation " + declaringSymbol);
    resolvedInitializers.clear();
    Collection<? extends Property> properties = getAllProperties();
    for (Property property : properties) {
      if (property.isInstanceField()) {
        System.out.println(" - instance field " + property.getName());
        classValidationContext.validateProperty(property);
        ((GenericProperty) property).fieldIndex = resolvedInitializers.size();
        resolvedInitializers.add(property.getInitializer());
      }
    }
    for (Property property : properties) {
      if (!property.isInstanceField()) {
        System.out.println(" - non instance field " + property.getName());
        classValidationContext.validateProperty(property);
      }
    }
  }

  @Override
  public Instance createInstance(EvaluationContext evaluationContext, Object... ctorValues) {
    int fieldCount = resolvedInitializers.size();
    Object[] properties = new Object[fieldCount];
    for (int i = 0; i < fieldCount; i++) {
      properties[i] = ctorValues !=null && ctorValues.length > i && ctorValues[i] != null ? ctorValues[i] : resolvedInitializers.get(i).eval(evaluationContext);
    }
    return new Instance(this, properties);
  }


  @Override
  public void setDeclaredBy(Property declaringSymbol) {
    this.declaringSymbol = declaringSymbol;
  }

  @Override
  public Property getDeclaringSymbol() {
    return declaringSymbol;
  }


  public void addSymbol(GenericProperty symbol) {
    propertyMap.put(symbol.getName(), (GenericProperty) symbol);
  }

  @Override
  public boolean hasDefaultValue() {
    return false;
  }

  @Override
  public Object getDefaultValue() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return declaringSymbol == null ? super.toString() : declaringSymbol.getName();
  }

  public void remove(String name) {
    propertyMap.remove(name);
  }

}
