package org.kobjects.asde.lang.function;

import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;

public class BuiltinFunction implements Callable {

  private final CharSequence documentation;
  private final FunctionType functionType;
  private final NativeImplementation callable;

  public BuiltinFunction(NativeImplementation callable, CharSequence documentation, Type returnType, Type... args) {
    this.callable = callable;
    this.documentation = documentation;
    this.functionType = FunctionType.createFromTypes(returnType, args);
  }

  @Override
  public CharSequence getDocumentation() {
    return documentation;
  }


  @Override
  public FunctionType getType() {
    return functionType;
  }

  @Override
  public Object call(EvaluationContext evaluationContext, int paramCount) {
    return callable.call(evaluationContext, paramCount);
  }

  @Override
  public Property getDeclaringSymbol() {
    return null;
  }

  public interface NativeImplementation {
    Object call(EvaluationContext evaluationContext, int paramCount);
  }

}
