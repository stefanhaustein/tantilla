package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;
import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;

public class Named extends ExpressionNode {

  public final String name;
  public Named(String name, ExpressionNode value) {
    super(value);
    this.name = name;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, name + " = ", errors);
    children[0].toString(asb, errors, preferAscii);
  }

  @Override
  protected Type resolveWasmImpl(WasmExpressionBuilder wasm, ValidationContext resolutionContext, int line) {
    throw new UnsupportedOperationException();
  }
}
