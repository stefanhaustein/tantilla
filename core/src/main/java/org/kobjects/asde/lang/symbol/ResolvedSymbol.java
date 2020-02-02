package org.kobjects.asde.lang.symbol;

import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.statement.DeclarationStatement;
import org.kobjects.asde.lang.list.ListType;
import org.kobjects.typesystem.Type;

public interface ResolvedSymbol {

  Object get(EvaluationContext evaluationContext);
  void set(EvaluationContext evaluationContext, Object value);
  Type getType();
  boolean isConstant();
}
