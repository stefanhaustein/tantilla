package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.program.GlobalSymbol;
import org.kobjects.asde.lang.property.PropertyDescriptor;
import org.kobjects.asde.lang.symbol.StaticSymbol;

import java.util.Collections;
import java.util.Map;

public abstract class AbstractUserClassProperty implements PropertyDescriptor, StaticSymbol {
  UserClass owner;
  String name;
  Map<Node, Exception> errors = Collections.emptyMap();

  AbstractUserClassProperty(UserClass owner, String name) {
    this.owner = owner;
    this.name = name;
  }

  abstract void validate(ClassValidationContext classValidationContext);

  @Override
  public String getName() {
    return name;
  }


  @Override
  public UserClass getOwner() {
    return owner;
  }

  @Override
  public Map<Node, Exception> getErrors() {
    return errors;
  }


  @Override
  public GlobalSymbol.Scope getScope() {
    return GlobalSymbol.Scope.PERSISTENT;
  }

  public abstract boolean isConstant();

}
