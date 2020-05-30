package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.function.Callable;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.function.LocalSymbol;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;

// Not static for access to the variables.
public class Identifier extends SymbolNode implements HasProperty {

  enum Kind {
    UNRESOLVED, LOCAL_VARIABLE, ROOT_MODULE_PROPERTY, ERROR, ROOT_METHOD_INVOCATION;
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
    resolvedRootProperty = null;
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
      if (resolvedRootProperty.getType() instanceof FunctionType) {
        FunctionType functionType = (FunctionType) resolvedRootProperty.getType();
        if (functionType.getParameterCount() != 0) {
          throw new RuntimeException("Function can't be called implicitly because it has parameters.");
        }
        resolvedKind = Kind.ROOT_METHOD_INVOCATION;
      } else {
        resolvedMutable = resolvedRootProperty.isMutable();
        resolvedKind = Kind.ROOT_MODULE_PROPERTY;
      }
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
      case ROOT_METHOD_INVOCATION:
      case ROOT_MODULE_PROPERTY:
        resolvedRootProperty.setStaticValue(value);
        break;
      default:
        throw new RuntimeException("Unassignable: " + name);
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
      case ROOT_METHOD_INVOCATION:
        Callable callable = (Callable) resolvedRootProperty.getStaticValue();
        evaluationContext.ensureExtraStackSpace(callable.getLocalVariableCount());
        return callable.call(evaluationContext, 0);
    }
    throw new RuntimeException("Unresolved variable "+ name);
  }

  public Type returnType() {
    switch (resolvedKind) {
      case LOCAL_VARIABLE:
        return resolvedLocalVariable.getType();
      case ROOT_MODULE_PROPERTY:
        return resolvedRootProperty.getType();
      case ROOT_METHOD_INVOCATION:
        return ((FunctionType) resolvedRootProperty.getType()).getReturnType();
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
    return resolvedRootProperty;
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
