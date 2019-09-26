package org.kobjects.asde.lang.statement;

import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.FunctionImplementation;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.asde.lang.ResolvedSymbol;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.typesystem.Type;

public class DefStatement extends Node {

  public final String name;
  public final FunctionImplementation implementation;
  ResolvedSymbol resolved;

  public DefStatement(String name, FunctionImplementation implementation) {
    this.name = name;
    this.implementation = implementation;
  }

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, int line, int index) {
    FunctionValidationContext innerContext = new FunctionValidationContext(resolutionContext.programValidationContext, resolutionContext.mode, implementation);
    implementation.validate(innerContext);
    resolved = resolutionContext.resolveVariableDeclaration(name, implementation.getType(), true);
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    resolved.set(evaluationContext, implementation);
    return null;
  }

  @Override
  public Type returnType() {
    return Types.VOID;
  }
}
