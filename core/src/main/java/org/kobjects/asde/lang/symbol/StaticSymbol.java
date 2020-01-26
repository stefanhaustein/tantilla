package org.kobjects.asde.lang.symbol;

import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.program.GlobalSymbol;
import org.kobjects.asde.lang.statement.Statement;
import org.kobjects.typesystem.Type;

import java.util.Map;

public interface StaticSymbol {
  SymbolOwner getOwner();
  Map<Node, Exception> getErrors();
  Object getValue();
  String getName();
  Type getType();
  Node getInitializer();
  void validate();

  GlobalSymbol.Scope getScope();

  boolean isConstant();

  void setName(String newName);

}
