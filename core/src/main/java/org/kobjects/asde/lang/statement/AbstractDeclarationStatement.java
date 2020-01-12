package org.kobjects.asde.lang.statement;

import org.kobjects.asde.lang.array.ArrayType;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.symbol.ResolvedSymbol;
import org.kobjects.asde.lang.symbol.StaticSymbol;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.typesystem.Type;

public abstract class AbstractDeclarationStatement extends Statement {

  String varName;
  ResolvedSymbol resolved;

  AbstractDeclarationStatement(String varName, Node... children) {
    super(children);
    this.varName = varName;
  }

  @Override
  public void rename(StaticSymbol symbol, String oldName, String newName) {
    if (symbol == resolved && oldName.equals(varName)) {
      varName = newName;
    }
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    resolved.set(evaluationContext, evalValue(evaluationContext));
    return null;
  }

  public abstract Object evalValue(EvaluationContext evaluationContext);

  public String getVarName() {
    return varName;
  }

  public abstract Type getValueType();

  public void setVarName(String newName) {
    this.varName = varName;
  }
}
