package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.function.LocalSymbol;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;

// Not static for access to the variables.
public class Identifier extends SymbolNode {

  enum Kind {
    UNRESOLVED, LOCAL_VARIABLE, ROOT_MODULE_PROPERTY, ERROR;
  }

  String name;

  Kind resolvedKind = Kind.UNRESOLVED;
  LocalSymbol resolvedLocalVariable;
  Property resolvedRootProperty;
  boolean resolvedMutable;
  public Identifier(String name) {
    this.name = name;
  }

  public void onResolve(ValidationContext resolutionContext, int line) {
    resolvedLocalVariable = resolutionContext.getCurrentBlock().get(name);
    resolvedKind = Kind.ERROR;
    if (resolvedLocalVariable != null) {
      resolvedMutable = resolvedLocalVariable.isMutable();
      resolvedKind = Kind.LOCAL_VARIABLE;
    } else {
      resolvedRootProperty = resolutionContext.program.mainModule.getProperty(name);
      if (resolvedRootProperty == null) {
        throw new RuntimeException("Variable not found: '" + name + "'");
      }
      resolutionContext.validateProperty(resolvedRootProperty);
      if (resolvedRootProperty.isInstanceField()) {
        // Modules can't have non-static properties...
        throw new IllegalStateException();
      }
      resolvedMutable = resolvedRootProperty.isMutable();
      resolvedKind = Kind.ROOT_MODULE_PROPERTY;
    }
  }

  @Override
  public Type resolveForAssignment(ValidationContext resolutionContext, int line) {
    resolve(resolutionContext, line);
    if (!resolvedMutable) {
      throw new RuntimeException("Can't assign to immutable variable '" + name + "'");
    }
    return returnType();
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
    return !resolvedMutable;
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
  public void rename(Property symbol) {
    if (resolvedKind == Kind.ROOT_MODULE_PROPERTY && symbol == resolvedRootProperty) {
      this.name = symbol.getName();
    }
  }

  @Override
  public Property getResolvedProperty() {
    return resolvedKind == Kind.ROOT_MODULE_PROPERTY ? resolvedRootProperty : null;
  }

  public void setName(String s) {
    name = s;
  }

  @Override
  public void renameParameters(Map<String, String> renameMap) {
    if (resolvedKind == Kind.LOCAL_VARIABLE) {
      String renameTo = renameMap.get(name);
      if (renameTo != null) {
        setName(renameTo);
      }
    }
  }
}
