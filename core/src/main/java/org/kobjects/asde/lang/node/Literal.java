package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Interpreter;

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
  public Class<?> returnType() {
    return value.getClass();
  }

  @Override
  public String toString() {
    if (value != Program.INVISIBLE_STRING && value instanceof String) {
      return "\"" + ((String) value).replace("\"", "\"\"") + '"';
    }
    return Program.toString(value);
  }
}
