package org.kobjects.asde.lang.symbol;

import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.program.GlobalSymbol;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.HashSet;
import java.util.Map;

public interface StaticSymbol {
  SymbolOwner getOwner();
  Map<Node, Exception> getErrors();
  Object getStaticValue();
  String getName();
  Type getType();
  Node getInitializer();
  void validate();

  GlobalSymbol.Scope getScope();

  boolean isConstant();

  void setName(String newName);

  void init(EvaluationContext evaluationContext, HashSet<StaticSymbol> initialized);
}
