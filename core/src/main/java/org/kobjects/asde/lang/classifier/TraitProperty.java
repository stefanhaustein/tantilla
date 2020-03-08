package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.function.Callable;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.type.Types;

import java.util.Collections;
import java.util.Map;

public class TraitProperty implements Property {
  private final Trait owner;
  private boolean mutable;
  private String name;
  private Type type;
  private Map<Node, Exception> errors = Collections.emptyMap();

  TraitProperty(Trait owner, boolean mutable, String name, Type type) {
    this.owner = owner;
    this.mutable = mutable;
    this.name = name;
    this.type = type;
  }

  @Override
  public Trait getOwner() {
    return owner;
  }

/*  @Override
  public Map<Node, Exception> getErrors() {
    return errors;
  }*/

  @Override
  public Object getStaticValue() {
    return new Callable() {
      @Override
      public Object call(EvaluationContext evaluationContext, int paramCount) {
        throw new RuntimeException("NYI");
      }

      @Override
      public FunctionType getType() {
        return (FunctionType) TraitProperty.this.getType();
      }

      @Override
      public Property getDeclaringSymbol() {
        return TraitProperty.this;
      }
    };
  }

  /*
  @Override
  public Node getInitializer() {
    return null;
  }*/


  @Override
  public boolean isMutable() {
    return mutable;
  }

  @Override
  public boolean isInstanceField() {
    // Hack
    return !(type instanceof FunctionType);
  }

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
    return ((Classifier) Types.of(instance)).getProperty(name).get(context, instance);
  }

  @Override
  public void set(EvaluationContext context, Object instance, Object value) {
    ((Classifier) Types.of(instance)).getProperty(name).set(context, instance, value);
  }

}
