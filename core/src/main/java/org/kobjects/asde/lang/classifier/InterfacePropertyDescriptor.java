package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.program.GlobalSymbol;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.symbol.ResolvedSymbol;
import org.kobjects.asde.lang.symbol.StaticSymbol;
import org.kobjects.asde.lang.symbol.SymbolOwner;
import org.kobjects.asde.lang.property.PropertyDescriptor;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.type.Types;

import java.util.Collections;
import java.util.Map;

public class InterfacePropertyDescriptor implements PropertyDescriptor, ResolvedSymbol, StaticSymbol {
  private final InterfaceImplementation owner;
  private String name;
  private Type type;
  private Map<Node, Exception> errors = Collections.emptyMap();

  InterfacePropertyDescriptor(InterfaceImplementation owner, String name, Type type) {
    this.owner = owner;
    this.name = name;
    this.type = type;
  }

  @Override
  public Object get(EvaluationContext evaluationContext) {
    throw new RuntimeException("NYI");
  }

  @Override
  public void set(EvaluationContext evaluationContext, Object value) {
    throw new RuntimeException("NYI");
  }

  @Override
  public SymbolOwner getOwner() {
    return owner;
  }

  @Override
  public Map<Node, Exception> getErrors() {
    return errors;
  }

  @Override
  public Object getValue() {
    return null;
  }

  @Override
  public Node getInitializer() {
    return null;
  }

  @Override
  public void validate() {

  }

  @Override
  public GlobalSymbol.Scope getScope() {
    return GlobalSymbol.Scope.PERSISTENT;
  }

  @Override
  public boolean isConstant() {
    return false;
  }

  @Override
  public void setName(String newName) {
    this.name = newName;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public Object get(EvaluationContext context, Object instance) {
    return ((Classifier) Types.of(instance)).getPropertyDescriptor(name).get(context, instance);
  }

  @Override
  public void set(EvaluationContext context, Object instance, Object value) {
    ((Classifier) Types.of(instance)).getPropertyDescriptor(name).set(context, instance, value);
  }
}
