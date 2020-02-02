package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.Types;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class Literal extends Node {
  public final Object value;
  private final Format format;

  public enum Format {
    DEFAULT,
    HEX
  }

  public Literal(Object value, Format format) {
    super((Node[]) null);
    this.value = value;
    this.format = format;
  }

  public Literal(Object value) {
    this(value, Format.DEFAULT);
  }

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, int line) {
    // Nothing to do here.
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    return value;
  }

  @Override
  public Type returnType() {
    return Types.of(value);
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    if (value != Program.INVISIBLE_STRING && value instanceof String) {
      appendLinked(asb, "\"" + ((String) value).replace("\"", "\"\"") + '"', errors);
    } else if (format == Format.HEX && returnType() == Types.FLOAT && ((Number) value).longValue() == ((Number) value).doubleValue()) {
      appendLinked(asb, "0x" + Long.toHexString(((Number) value).longValue()), errors);
    } else {
      appendLinked(asb, Program.toString(value), errors);
    }
  }
}
