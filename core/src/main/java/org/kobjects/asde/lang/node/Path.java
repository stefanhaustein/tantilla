package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.function.Callable;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.type.EnumType;
import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.type.MetaType;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;


public class Path extends SymbolNode {
  enum ResolvedKind {
    INSTANCE_FIELD,
    INSTANCE_PROPERTY_GET,
    STATIC_PROPERTY,
    ENUM_LITERAL,
    UNRESOLVED,
    SET_METHOD_CALL,
    ERROR
  }

  public String pathName;
  private Property resolvedProperty;
  private Object resolvedConstant;
  private ResolvedKind resolvedKind = ResolvedKind.UNRESOLVED;

  public Path(Node left, Node right) {
    super(left);
    if (!(right instanceof Identifier)) {
      throw new RuntimeException("Path name expected");
    }
    pathName = ((Identifier) right).name;
  }

  @Override
  protected void onResolve(ValidationContext resolutionContext, int line) {
    onResolve(resolutionContext, line, false);
  }

  private Type onResolve(ValidationContext resolutionContext, int line, boolean forSet) {
    resolvedKind = ResolvedKind.ERROR;
    if (children[0].returnType() instanceof Classifier) {
      if (forSet) {
        // TODO: look up property first and check for consistency!
        boolean inside_set = children[0].toString().equals("self")
            && resolutionContext.property != null
            && resolutionContext.property.getName().equals("set_" + pathName);

        if (!inside_set) {
          resolvedProperty = ((Classifier) children[0].returnType()).getProperty("set_" + pathName);

          // We check for an exact match, as we still need to fall through to the property check
          if (resolvedProperty != null && !resolvedProperty.isInstanceField() && resolvedProperty.getType() instanceof FunctionType) {
            FunctionType functionType = (FunctionType) resolvedProperty.getType();
            if (functionType.getParameterCount() == 2
                && functionType.getParameter(0).getName().equals("self")
              // TODO: && ...
            ) {
              resolvedKind = ResolvedKind.SET_METHOD_CALL;
              return functionType.getParameter(1).getType();
            }
          }
        }
      }

      resolvedProperty = ((Classifier) children[0].returnType()).getProperty(pathName);
      if (resolvedProperty == null) {
        throw new RuntimeException("Property '" + pathName + "'"
            + (forSet ? (" or set_" + pathName + " method") : "")
            + " not found in " + children[0].returnType());
      }
      resolutionContext.validateProperty(resolvedProperty);
      if (resolvedProperty.getType() == null) {
        throw new RuntimeException("Type of property '" + resolvedProperty + "' is null.");
      }
      if (!resolvedProperty.isInstanceField()) {
        if (resolvedProperty.getType() instanceof FunctionType) {
          FunctionType functionType = (FunctionType) resolvedProperty.getType();
          if (functionType.getParameterCount() != 1 || !functionType.getParameter(0).getName().equals("self")) {
            throw new RuntimeException("Parameterless (apart from self) method expected for property-style invocation.");
          }
          resolvedKind = ResolvedKind.INSTANCE_PROPERTY_GET;
          return functionType.getReturnType();
        }
        throw new RuntimeException("No-static Instance property expected. Please use " + children[0].returnType() + "." + resolvedProperty + " for a static reference instead.");
      }
      resolvedKind = ResolvedKind.INSTANCE_FIELD;
      if (forSet && !resolvedProperty.isMutable()) {
        throw new RuntimeException("Not mutable.");
      }
      return resolvedProperty.getType();
    }

    if (children[0].returnType() instanceof MetaType) {
      Type type = ((MetaType) children[0].returnType()).getWrapped();
      if (type instanceof EnumType) {
        if (forSet) {
          throw new RuntimeException("Can't assign to enum literal.");
        }
        EnumType enumType = (EnumType) type;
        resolvedConstant = enumType.getLiteral(pathName);
        resolvedKind = ResolvedKind.ENUM_LITERAL;
        return enumType;
      }
      if (type instanceof Classifier) {
        resolvedProperty = ((Classifier) type).getProperty(pathName);
        if (resolvedProperty == null) {
          throw new RuntimeException("Property '" + pathName + "' not found in " + children[0].returnType());
        }
        resolutionContext.validateProperty(resolvedProperty);
        if (resolvedProperty.getType() == null) {
          throw new RuntimeException("Type of property '" + resolvedProperty + "' is null.");
        }
        if (resolvedProperty.isInstanceField()) {
          throw new RuntimeException("Static property expected for static reference.");
        }
        resolvedKind = ResolvedKind.STATIC_PROPERTY;
        if (forSet && !resolvedProperty.isMutable()) {
          throw new RuntimeException("Not mutable.");
        }
        return resolvedProperty.getType();
      }
    }
    throw new RuntimeException("Classifier expected as path base; got: " + children[0].returnType());
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    switch (resolvedKind) {
      case INSTANCE_FIELD: {
        Object instance = children[0].eval(evaluationContext);
        if (instance == null) {
          throw new EvaluationException(this, "path base is null");
        }
        return resolvedProperty.get(evaluationContext, instance);
      }
      case ENUM_LITERAL:
        return resolvedConstant;
      case STATIC_PROPERTY:
        return resolvedProperty.getStaticValue();
      case INSTANCE_PROPERTY_GET:
        Object instance = children[0].eval(evaluationContext);
        if (instance == null) {
          throw new EvaluationException(this, "path base is null");
        }
        Callable callable = (Callable) resolvedProperty.getStaticValue();
        evaluationContext.ensureExtraStackSpace(callable.getLocalVariableCount());
        evaluationContext.push(instance);
        return callable.call(evaluationContext, 1);

    }
    throw new IllegalStateException(resolvedKind + ": " + this);
  }

  @Override
  public Type returnType() {
    switch (resolvedKind) {
      case INSTANCE_FIELD:
      case STATIC_PROPERTY:
        return resolvedProperty.getType();
      case ENUM_LITERAL:
        return Types.of(resolvedConstant);
      case INSTANCE_PROPERTY_GET:
        return ((FunctionType) resolvedProperty.getType()).getReturnType();
    }
    return null;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    children[0].toString(asb, errors, preferAscii);
    appendLinked(asb, "." + pathName, errors);
  }

  @Override
  public Type resolveForAssignment(ValidationContext resolutionContext, int line) {
    children[0].resolve(resolutionContext, line);
    return onResolve(resolutionContext, line, true);
  }


  @Override
  public void set(EvaluationContext evaluationContext, Object value) {
    switch (resolvedKind) {
      case STATIC_PROPERTY:
        resolvedProperty.setStaticValue(value);
        break;
      case INSTANCE_FIELD:
        Object instance = children[0].eval(evaluationContext);
        resolvedProperty.set(evaluationContext, instance, value);
        break;
      case SET_METHOD_CALL:
        Callable callable = (Callable) resolvedProperty.getStaticValue();
        evaluationContext.ensureExtraStackSpace(callable.getLocalVariableCount());
        evaluationContext.push(children[0].eval(evaluationContext));
        evaluationContext.push(value);
        callable.call(evaluationContext, 2);
        break;
      default:
        throw new IllegalStateException(resolvedKind + ": " + this);
    }
  }

  @Override
  public boolean isConstant() {
    return resolvedConstant != null;
  }

  @Override
  public Property getResolvedProperty() {
    return resolvedProperty;
  }

}
