package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.ResolutionContext;
import org.kobjects.asde.lang.Symbol;
import org.kobjects.asde.lang.type.Type;

// Not static for access to the variables.
public class Identifier extends AssignableNode {
  final Program program;
  public final String name;
  Symbol resolved;

  public Identifier(Program program, String name) {
    this.program = program;
    this.name = name;
  }

  public void resolve(ResolutionContext resolutionContext) {
      super.resolve(resolutionContext);
      resolved = resolutionContext.getSymbol(name);
      if (resolved == null) {
        throw new RuntimeException("Symbol not found: " + name);
      }
  }

  public void set(Interpreter interpreter, Object value) {
    Symbol symbol = resolved == null ? program.getSymbol(name) : resolved;
    if (symbol == null) {
      symbol = new Symbol(interpreter.getSymbolScope(), value);
      program.setSymbol(name, symbol);
    } else {
      symbol.value = value;
    }
  }

  @Override
  public Object eval(Interpreter interpreter) {
    Object result = evalRaw(interpreter);
    return result == null ? name.endsWith("$") ? "" : 0.0 : result;
  }

  @Override
  public Object evalRaw(Interpreter interpreter) {
    Symbol symbol = resolved != null ? resolved : program.getSymbol(name);
    return symbol == null ? null : symbol.value;
  }

  public Type returnType() {
    return resolved.type;
  }

  public String toString() {
    return name;
  }
}
