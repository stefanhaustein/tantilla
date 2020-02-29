package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.classifier.UserProperty;
import org.kobjects.asde.lang.function.LocalSymbol;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;

// Not static for access to the variables.
public class Identifier extends SymbolNode {
  enum Kind {
    UNRESOLVED, LOCAL_VARIABLE, ROOT_MODULE_PROPERTY;
  }

  String name;

  Kind resolvedKind = Kind.UNRESOLVED;
  LocalSymbol resolvedLocalVariable;
  Property resolvedRootProperty;
  boolean mutable;

  public Identifier(String name) {
    this.name = name;
  }

  public void onResolve(ValidationContext resolutionContext, int line) {
    resolvedLocalVariable = resolutionContext.getCurrentBlock().get(name);
    if (resolvedLocalVariable != null) {
      resolvedKind = Kind.LOCAL_VARIABLE;
      mutable = resolvedLocalVariable.isMutable();
    } else {
      resolvedRootProperty = resolutionContext.program.mainModule.getProperty(name);
      if (resolvedRootProperty == null) {
        resolvedKind = Kind.UNRESOLVED;
        throw new RuntimeException("Variable not found: '" + name + "'");
      }
      resolutionContext.validateAndAddDependency(resolvedRootProperty);
      if (resolvedRootProperty.isInstanceField()) {
        resolvedKind = Kind.UNRESOLVED;
        // Modules can't have non-static properties...
        throw new IllegalStateException();
      }
      mutable = resolvedRootProperty.isMutable();
      resolvedKind = Kind.ROOT_MODULE_PROPERTY;
    }
  }

  @Override
  public void resolveForAssignment(ValidationContext resolutionContext, Type type, int line) {
    onResolve(resolutionContext, line);
    if (!mutable) {
      throw new RuntimeException("Can't assign to immutable variable '" + name + "'");
    }
    if (!type.isAssignableFrom(returnType())) {
      throw new RuntimeException("Can't assign a value of type " + type + " to '" + name + "' of type " + returnType());
    }
  }

  public void set(EvaluationContext evaluationContext, Object value) {
    switch (resolvedKind) {
      case LOCAL_VARIABLE:
        resolvedLocalVariable.set(evaluationContext, value);
        break;
      case ROOT_MODULE_PROPERTY:
        resolvedRootProperty.setStaticValue(value);
        break;
      default:
        throw new RuntimeException("Unresolved: " + name);
    }
  }

  @Override
  public boolean isConstant() {
    return !mutable;
  }


  @Override
  public Object eval(EvaluationContext evaluationContext) {
    switch (resolvedKind) {
      case LOCAL_VARIABLE:
        return resolvedLocalVariable.get(evaluationContext);
      case ROOT_MODULE_PROPERTY:
        return resolvedRootProperty.getStaticValue();
    }
    throw new RuntimeException("Unresolved variable "+ name);
  }

  public Type returnType() {
    switch (resolvedKind) {
      case LOCAL_VARIABLE:
        return resolvedLocalVariable.getType();
      case ROOT_MODULE_PROPERTY:
        return resolvedRootProperty.getType();
    }
    return null;
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
    return false;
  }

  public void setName(String s) {
    name = s;
  }
}
