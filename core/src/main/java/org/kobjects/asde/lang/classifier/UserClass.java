package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.symbol.Declaration;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.FunctionImplementation;
import org.kobjects.asde.lang.program.GlobalSymbol;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.symbol.StaticSymbol;
import org.kobjects.asde.lang.symbol.SymbolOwner;
import org.kobjects.asde.lang.statement.AbstractDeclarationStatement;
import org.kobjects.asde.lang.type.MetaType;
import org.kobjects.asde.lang.property.PhysicalProperty;
import org.kobjects.asde.lang.property.Property;
import org.kobjects.asde.lang.property.PropertyDescriptor;
import org.kobjects.asde.lang.type.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

public class UserClass implements Classifier, InstantiableType, Declaration, SymbolOwner {

  final Program program;
  public final TreeMap<String, AbstractUserClassProperty> propertyMap = new TreeMap<>();
  ArrayList<AbstractDeclarationStatement> resolvedInitializers = new ArrayList<>();
  GlobalSymbol declaringSymbol;

  public UserClass(Program program) {
    this.program = program;
  }


  @Override
  public AbstractUserClassProperty getPropertyDescriptor(String name) {
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
  public Type getType() {
    return new MetaType(this);
  }

  public void setMethod(String functionName, FunctionImplementation methodImplementation) {
    propertyMap.put(functionName, new UserMethod(this, functionName, methodImplementation));
  }

  public void setProperty(String propertyName, AbstractDeclarationStatement initializer) {
    propertyMap.put(propertyName, new UserClassProperty(this, propertyName, initializer));
  }

  public void processDeclaration(AbstractDeclarationStatement declaration) {
    propertyMap.put(declaration.getVarName(), new UserClassProperty(this, declaration.getVarName(), declaration));
  }

  public void validate(ClassValidationContext classValidationContext) {
    resolvedInitializers.clear();
    for (AbstractUserClassProperty propertyDescriptor : propertyMap.values()) {
      propertyDescriptor.validate(classValidationContext);
    }
  }

  @Override
  public Instance createInstance(EvaluationContext evaluationContext, Object... ctorValues) {
    int fieldCount = resolvedInitializers.size();
    Property[] properties = new Property[fieldCount];
    for (int i = 0; i < fieldCount; i++) {
      properties[i] = new PhysicalProperty(ctorValues !=null && ctorValues.length > i && ctorValues[i] != null ? ctorValues[i] : resolvedInitializers.get(i).evalValue(evaluationContext));
    }
    return new InstanceImpl(this, properties);
  }


  @Override
  public void setDeclaringSymbol(StaticSymbol declaringSymbol) {
    this.declaringSymbol = (GlobalSymbol) declaringSymbol;
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
    propertyMap.put(symbol.getName(), (UserClassProperty) symbol);
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
