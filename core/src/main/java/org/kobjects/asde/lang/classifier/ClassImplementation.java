package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.symbol.Declaration;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.FunctionImplementation;
import org.kobjects.asde.lang.program.GlobalSymbol;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.symbol.StaticSymbol;
import org.kobjects.asde.lang.symbol.SymbolOwner;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.statement.AbstractDeclarationStatement;
import org.kobjects.asde.lang.statement.DeclarationStatement;
import org.kobjects.asde.lang.function.CodeLine;
import org.kobjects.typesystem.Instance;
import org.kobjects.typesystem.InstanceType;
import org.kobjects.typesystem.MetaType;
import org.kobjects.typesystem.PhysicalProperty;
import org.kobjects.typesystem.Property;
import org.kobjects.typesystem.PropertyDescriptor;
import org.kobjects.typesystem.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

public class ClassImplementation implements InstanceType, InstantiableType, Declaration, SymbolOwner {

  final Program program;
  public final TreeMap<String, ClassPropertyDescriptor> propertyMap = new TreeMap<>();
  ArrayList<AbstractDeclarationStatement> resolvedInitializers = new ArrayList<>();
  GlobalSymbol declaringSymbol;

  public ClassImplementation(Program program) {
    this.program = program;
  }


  @Override
  public ClassPropertyDescriptor getPropertyDescriptor(String name) {
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
    propertyMap.put(functionName, new ClassPropertyDescriptor(this, functionName, methodImplementation));
  }

  public void setProperty(String propertyName, AbstractDeclarationStatement initializer) {
    propertyMap.put(propertyName, new ClassPropertyDescriptor(this, propertyName, initializer));
  }

  public void processDeclarations(CodeLine codeLine) {
    for (int i = 0; i < codeLine.length(); i++) {
      Node node = codeLine.get(i);
      if (node instanceof DeclarationStatement) {
        DeclarationStatement declaration = (DeclarationStatement) node;
        propertyMap.put(declaration.getVarName(), new ClassPropertyDescriptor(this, declaration.getVarName(), declaration));
      } else {
        throw new RuntimeException("Unsupported declaration in class: " + node);
      }
    }
  }

  public void validate(ClassValidationContext classValidationContext) {
    resolvedInitializers.clear();
    for (ClassPropertyDescriptor propertyDescriptor : propertyMap.values()) {
      propertyDescriptor.validate(classValidationContext);
    }
  }

  @Override
  public Instance createInstance(EvaluationContext evaluationContext) {
    int fieldCount = resolvedInitializers.size();
    Property[] properties = new Property[fieldCount];
    for (int i = 0; i < fieldCount; i++) {
      properties[i] = new PhysicalProperty(resolvedInitializers.get(i).evalValue(evaluationContext));
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
    propertyMap.put(symbol.getName(), (ClassPropertyDescriptor) symbol);
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
