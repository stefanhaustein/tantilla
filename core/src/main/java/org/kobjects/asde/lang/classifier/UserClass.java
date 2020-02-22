package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.function.Callable;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.symbol.Declaration;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.symbol.StaticSymbol;
import org.kobjects.asde.lang.symbol.SymbolOwner;
import org.kobjects.asde.lang.type.MetaType;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.type.Types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

public class UserClass implements Classifier, InstantiableType, Declaration, SymbolOwner {

  final Program program;
  public final TreeMap<String, Property> propertyMap = new TreeMap<>();
  ArrayList<Node> resolvedInitializers = new ArrayList<>();
  StaticSymbol declaringSymbol;

  public UserClass(Program program) {
    this.program = program;
  }

  public Collection<UserProperty> getUserProperties() {
    ArrayList<UserProperty> userProperties = new ArrayList<>();
    for (Property property : propertyMap.values()) {
      if (property instanceof UserProperty) {
        userProperties.add((UserProperty) property);
      }
    }
    return userProperties;
  }

  @Override
  public Property getPropertyDescriptor(String name) {
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
  public Type getType() {
    return new MetaType(this);
  }

  void setProperty(
      boolean isInstanceField,
      boolean isMutable,
      Type fixedType,
      String name,
      Node initializer,
      Object staticValue) {
    propertyMap.put(name, new UserProperty(
            this,
            isInstanceField,
            isMutable,
            fixedType,
            name,
            initializer,
            staticValue));
    program.notifyProgramChanged();
  }


  public void setMethod(String functionName, Callable methodImplementation) {
    setProperty(
        /* isInstanceField= */ false,
        /* isMutable= */ false,
        methodImplementation.getType(),
        functionName,
        /* initializer= */ null,
        methodImplementation);
  }

  public void setProperty(String propertyName, Node initializer) {
    setProperty(
        /* isInstanceField= */ true,
        /* isMutable= */ true,
        /* fixedType= */ null,
        propertyName,
        initializer,
        /* staticValue */ null);
  }

  public void setUninitializedProperty(String propertyName, Type type) {
    setProperty(
        /* isInstanceField= */ true,
        /* isMutable */ true,
        type,
        propertyName,
        /* initializer= */ null,
        /* staticValue= */ null);
  }

  public void setStaticValue(String propertyName, Object value) {
    setProperty(
        /* isInstanceField= */ false,
        /* isMutable */ false,
        Types.of(value),
        propertyName,
        /* initializer= */ null,
        /* staticValue= */ value);
  }


  public void validate(ClassValidationContext classValidationContext) {
    resolvedInitializers.clear();
    Collection<UserProperty> userProperties = getUserProperties();
    for (UserProperty property : userProperties) {
      if (property.isInstanceField) {
          property.validate(classValidationContext);
      }
    }
    for (UserProperty property : userProperties) {
      if (!property.isInstanceField) {
        property.validate(classValidationContext);
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
  public void setDeclaringSymbol(StaticSymbol declaringSymbol) {
    this.declaringSymbol = declaringSymbol;
  }

  @Override
  public StaticSymbol getSymbol(String name) {
    return (StaticSymbol) propertyMap.get(name);
  }

  @Override
  public void removeSymbol(StaticSymbol symbol) {
    propertyMap.remove(symbol.getName());
  }

  @Override
  public void addSymbol(StaticSymbol symbol) {
    propertyMap.put(symbol.getName(), (UserProperty) symbol);
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

}