package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.expression.ExpressionNode;
import org.kobjects.asde.lang.expression.Node;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;
import org.kobjects.asde.lang.wasm.runtime.WasmExpression;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractProperty implements Property {
  protected String name;
  protected boolean mutable;
  protected ExpressionNode initializer;
  protected Type fixedType;
  protected Type resolvedType;
  WasmExpression resolvedInitializer;

  Map<Node, Exception> errors = Collections.emptyMap();
  Set<Property> initializationDependencies = Collections.emptySet();

  protected AbstractProperty(boolean mutable, String name, Type fixedType, ExpressionNode initializer) {
    this.mutable = mutable;
    this.name = name;
    this.fixedType = fixedType;
    this.initializer = initializer;
  }

  @Override
  public void setDependenciesAndErrors(HashSet<Property> dependencies, HashMap<Node, Exception> errors) {
    this.initializationDependencies = dependencies;
    this.errors = errors;
  }

  @Override
  public String getName() {
    return name;
  }

  public ExpressionNode getInitializer() {
    if (initializer == null) {
      throw new IllegalStateException("Initializer not set.");
    }
    return initializer;
  }

  public boolean hasInitializer() {
    return initializer != null;
  }

  public void resolveInitializer(ValidationContext context) {
    if (initializer == null) {
      resolvedInitializer = null;
      resolvedType = null;
    } else {
      WasmExpressionBuilder builder = new WasmExpressionBuilder();
      resolvedType = initializer.resolveWasm(builder, context, 0);
      resolvedInitializer = builder.build();
    }
  }

  @Override
  public Set<Property> getInitializationDependencies() {
    return initializationDependencies;
  }


  @Override
  public boolean isMutable() {
    return mutable;
  }


  @Override
  public String toString() {
    return Property.toString(this);
  }
}
