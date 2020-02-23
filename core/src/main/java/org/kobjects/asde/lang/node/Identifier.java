package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.classifier.UserProperty;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.PropertyValidationContext;
import org.kobjects.asde.lang.symbol.ResolvedSymbol;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;

// Not static for access to the variables.
public class Identifier extends SymbolNode {
  String name;
  ResolvedSymbol resolved;

  public Identifier(String name) {
    this.name = name;
  }

  public void onResolve(PropertyValidationContext resolutionContext, int line) {
    resolved = resolutionContext.resolveVariableAccess(name);
  }

  @Override
  public void resolveForAssignment(PropertyValidationContext resolutionContext, Type type, int line) {
    resolved = resolutionContext.resolveVariableAssignment(name, type);
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
    return resolved.get(evaluationContext);
  }

  public Type returnType() {
    if (resolved == null) {
      throw new RuntimeException("Unresolved: " + name);
    }
    return  resolved.getType();
  }

  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, name, errors);
  }


  public String getName() {
    return name;
  }


  @Override
  public void rename(UserProperty symbol, String oldName, String newName) {
    if (matches(symbol, oldName)) {
      this.name = newName;
    }
  }

  @Override
  public boolean matches(UserProperty symbol, String name) {
    return resolved instanceof UserProperty && this.name.equals(name);
  }

  public void setName(String s) {
    name = s;
  }
}
