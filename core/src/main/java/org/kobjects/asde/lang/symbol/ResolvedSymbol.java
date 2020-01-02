package org.kobjects.asde.lang.symbol;

import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.statement.DeclarationStatement;
import org.kobjects.asde.lang.statement.DefStatement;
import org.kobjects.asde.lang.statement.DimStatement;
import org.kobjects.asde.lang.array.ArrayType;
import org.kobjects.typesystem.Type;

public interface ResolvedSymbol {

  static Type getInitializerType(Node initializer) {
    if (initializer instanceof DimStatement) {
      DimStatement dimStatement = (DimStatement) initializer;
      Type elementType = dimStatement.elementType;
      return new ArrayType(elementType, dimStatement.children.length);
    }
    if (initializer instanceof DeclarationStatement) {
      return initializer.children[0].returnType();
    }
    if (initializer instanceof DefStatement) {
      return ((DefStatement) initializer).implementation.getType();
    }
    throw new RuntimeException("not an initializer statement: " + initializer);
  }

  Object get(EvaluationContext evaluationContext);
  void set(EvaluationContext evaluationContext, Object value);
  Type getType();
  boolean isConstant();
}
