package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.asde.lang.ResolvedSymbol;
import org.kobjects.typesystem.Type;

import java.util.Map;

// Not static for access to the variables.
public class Identifier extends AssignableNode {
  final Program program;
  String name;
  ResolvedSymbol resolved;

  public Identifier(Program program, String name) {
    this.program = program;
    this.name = name;
  }

  public void onResolve(FunctionValidationContext resolutionContext, int line, int index) {
      resolved = resolutionContext.resolve(name);
  }

  public void set(EvaluationContext evaluationContext, Object value) {
    resolved.set(evaluationContext, value);
  }

  @Override
  public boolean isConstant() {
    return resolved.isConstant();
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    Object result = evalRaw(evaluationContext);
    return result == null ? name.endsWith("$") ? "" : 0.0 : result;
  }

  @Override
  public Object evalRaw(EvaluationContext evaluationContext) {
    return resolved.get(evaluationContext);
  }

  public Type returnType() {
    return resolved == null ? null : resolved.getType();
  }

  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
    appendLinked(asb, name, errors);
  }

  public void accept(Visitor visitor) {
    visitor.visitIdentifier(this);
  }

  public String getName() {
    return name;
  }

  public ResolvedSymbol getResolved() {
    return resolved;
  }

  public void setName(String name) {
    this.name = name;
  }
}
