package org.kobjects.asde.lang;

import org.kobjects.asde.lang.node.Node;
import org.kobjects.typesystem.Type;

import java.util.Map;

public interface StaticSymbol {
  Map<Node, Exception> getErrors();
  Object getValue();
  String getName();
  Type getType();
  Node getInitializer();
  void validate();

  GlobalSymbol.Scope getScope();

  boolean isConstant();
}
