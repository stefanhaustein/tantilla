package org.kobjects.asde.lang.function;

import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.FunctionTypeImpl;
import org.kobjects.typesystem.Type;

public class BuiltinFunction implements Function {

  private final CharSequence documentation;
  private final FunctionType functionType;
  private final Callable callable;

  @Override
  public FunctionType getType() {
    return functionType;
  }

  @Override
  public Object call(EvaluationContext evaluationContext, int paramCount) {
    return callable.call(evaluationContext, paramCount);
  }

  public BuiltinFunction(Callable callable, CharSequence documentation, Type returnType, Type... args) {
    this.callable = callable;
    this.documentation = documentation;
    this.functionType = new FunctionTypeImpl(returnType, args);
  }

  @Override
  public CharSequence getDocumentation() {
    return documentation;
  }


}
