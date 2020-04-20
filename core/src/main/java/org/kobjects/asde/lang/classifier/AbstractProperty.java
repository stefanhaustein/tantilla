package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.type.Type;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractProperty implements Property {
  protected String name;
  protected boolean mutable;
  protected Node initializer;
  protected Type fixedType;

  Map<Node, Exception> errors = Collections.emptyMap();
  Set<Property> initializationDependencies = Collections.emptySet();

  protected AbstractProperty(boolean mutable, String name, Type fixedType, Node initializer) {
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

  public Node getInitializer() {
    return initializer;
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
