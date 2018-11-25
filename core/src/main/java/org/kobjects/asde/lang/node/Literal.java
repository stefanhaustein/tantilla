package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Types;
import org.kobjects.asde.lang.parser.ResolutionContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class Literal extends Node {
  private final Object value;
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
  protected void onResolve(ResolutionContext resolutionContext) {
    // Nothing to do here.
  }

  @Override
  public Object eval(Interpreter interpreter) {
    return value;
  }

  @Override
  public Type returnType() {
    return value instanceof String ? Types.STRING : value instanceof Boolean ? Types.BOOLEAN : Types.NUMBER;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
    if (value != Program.INVISIBLE_STRING && value instanceof String) {
      appendLinked(asb, "\"" + ((String) value).replace("\"", "\"\"") + '"', errors);
    } else if (format == Format.HEX && returnType() == Types.NUMBER && ((Number) value).longValue() == ((Number) value).doubleValue()) {
      appendLinked(asb, "#" + Long.toHexString(((Number) value).longValue()), errors);
    } else {
      appendLinked(asb, Program.toString(value), errors);
    }
  }
}
