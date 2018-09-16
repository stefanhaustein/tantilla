package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.parser.ResolutionContext;
import org.kobjects.asde.lang.symbol.GlobalSymbol;
import org.kobjects.asde.lang.symbol.ResolvedSymbol;
import org.kobjects.typesystem.Type;

import java.util.Map;

// Not static for access to the variables.
public class Identifier extends AssignableNode {
  final Program program;
  public final String name;
  ResolvedSymbol resolved;

  public Identifier(Program program, String name) {
    this.program = program;
    this.name = name;
  }

  public void resolve(ResolutionContext resolutionContext) {
      super.resolve(resolutionContext);
      resolved = resolutionContext.resolve(name);
  }

  public void set(Interpreter interpreter, Object value) {
    resolved.set(interpreter, value);
  }

  @Override
  public Object eval(Interpreter interpreter) {
    Object result = evalRaw(interpreter);
    return result == null ? name.endsWith("$") ? "" : 0.0 : result;
  }

  @Override
  public Object evalRaw(Interpreter interpreter) {
    return resolved.get(interpreter);
  }

  public Type returnType() {
    return resolved == null ? null : resolved.getType();
  }

  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
    appendLinked(asb, name, errors);
  }
}
