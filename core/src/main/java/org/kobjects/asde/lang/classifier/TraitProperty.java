package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.function.PropertyValidationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.type.Types;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

public class TraitProperty implements Property {
  private final Trait owner;
  private String name;
  private Type type;
  private Map<Node, Exception> errors = Collections.emptyMap();

  TraitProperty(Trait owner, String name, Type type) {
    this.owner = owner;
    this.name = name;
    this.type = type;
  }

/*  @Override
  public Map<Node, Exception> getErrors() {
    return errors;
  }*/

  @Override
  public Object getStaticValue() {
    return null;
  }

  /*
  @Override
  public Node getInitializer() {
    return null;
  }*/


  @Override
  public boolean isMutable() {
    return isInstanceField();
  }

  @Override
  public boolean isInstanceField() {
    // Hack
    return !(type instanceof FunctionType);
  }

  public void setName(String newName) {
    this.name = newName;
  }

  public void init(EvaluationContext evaluationContext, HashSet<UserProperty> initialized) {

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



  @Override
  public void validate(PropertyValidationContext parentValidationContext) {
  }
}
