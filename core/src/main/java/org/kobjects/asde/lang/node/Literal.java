package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Types;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class Literal extends Node {
  Object value;

  public Literal(Object value) {
    super((Node[]) null);
    this.value = value;
  }

  @Override
  public Object eval(Interpreter interpreter) {
    return value;
  }

  @Override
  public Type returnType() {
    return value instanceof String ? Types.STRING : Types.NUMBER;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
    if (value != Program.INVISIBLE_STRING && value instanceof String) {
      appendLinked(asb, "\"" + ((String) value).replace("\"", "\"\"") + '"', errors);
    } else {
      appendLinked(asb, Program.toString(value), errors);
    }
  }
}
