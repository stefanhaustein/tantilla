package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.function.Callable;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.EnumType;
import org.kobjects.asde.lang.type.MetaType;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;
import org.kobjects.markdown.AnnotatedStringBuilder;

import java.util.Map;


public class PathWasm extends AssignableWasmNode implements HasProperty {
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

  public PathWasm(Node left, Node right) {
    super(left);
    if (!(right instanceof Identifier)) {
      throw new RuntimeException("Path name expected");
    }
    pathName = ((Identifier) right).name;
  }

  @Override
  public Type resolveWasmImpl(WasmExpressionBuilder wasm, ValidationContext resolutionContext, int line) {
    return resolveImpl(wasm, resolutionContext, line, /* forSet= */ false);
  }

  @Override
  public Type resolveForAssignment(WasmExpressionBuilder wasm, ValidationContext validationContext, int line) {
    return resolveImpl(wasm, validationContext, line, /* forSet= */ true);
  }

  private Type resolveImpl(WasmExpressionBuilder wasm, ValidationContext resolutionContext, int line, boolean forSet) {
    resolvedKind = ResolvedKind.ERROR;
    Type baseType = resolveWasm(new WasmExpressionBuilder(), resolutionContext, line);

    if (baseType instanceof Classifier) {
      return resolveClassifierProperty(wasm, resolutionContext, line, (Classifier) baseType, forSet);
    }
    if (baseType instanceof MetaType) {
      return resolveStatic(wasm, resolutionContext, line, ((MetaType) baseType).getWrapped(), forSet);
    }
    throw new RuntimeException("Classifier expected as path base; got: " + baseType);
  }

  private Type resolveClassifierProperty(WasmExpressionBuilder wasm, ValidationContext resolutionContext, int line, Classifier baseType, boolean forSet) {
    // For real this time.
    children[0].resolveWasm(wasm, resolutionContext, line);

    if (forSet) {
      // TODO: look up property first and check for consistency!
      boolean inside_set = children[0].toString().equals("self")
              && resolutionContext.property != null
              && resolutionContext.property.getName().equals("set_" + pathName);

      if (!inside_set) {
        resolvedProperty = baseType.getProperty("set_" + pathName);

        // We check for an exact match, as we still need to fall through to the property check
        if (resolvedProperty != null && !resolvedProperty.isInstanceField() && resolvedProperty.getType() instanceof FunctionType) {
          FunctionType functionType = (FunctionType) resolvedProperty.getType();
          if (functionType.getParameterCount() == 2
                  && functionType.getParameter(0).getName().equals("self")
            // TODO: && ...
          ) {
            resolvedKind = ResolvedKind.SET_METHOD_CALL;

            wasm.callWithContext(evaluationContext -> {
              Object base = evaluationContext.dataStack.popObject();
              Object value = evaluationContext.dataStack.popObject();
              Callable callable = (Callable) resolvedProperty.getStaticValue();
              evaluationContext.push(base);
              evaluationContext.push(value);
              evaluationContext.call(callable, 2);
            });
            return functionType.getParameter(1).getType();
          }
        }
      }
    }

    resolvedProperty = baseType.getProperty(pathName);
    if (resolvedProperty == null) {
      throw new RuntimeException("Property '" + pathName + "'"
          + (forSet ? (" or set_" + pathName + " method") : "")
          + " not found in " + children[0].returnType());
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
    if (forSet) {
      if (!resolvedProperty.isMutable()) {
        throw new RuntimeException("Not mutable.");
      }
      wasm.callWithContext(evaluationContext -> {
        Object instance = evaluationContext.dataStack.popObject();
        Object value = evaluationContext.dataStack.popObject();
        resolvedProperty.set(evaluationContext, instance, value);
      });
    } else {
      wasm.callWithContext(evaluationContext -> {
        evaluationContext.dataStack.pushObject(resolvedProperty.get(evaluationContext, evaluationContext.dataStack.popObject()));
      });
    }
    return resolvedProperty.getType();
  }

  private Type resolveStatic(WasmExpressionBuilder wasm, ValidationContext resolutionContext, int line, Type type, boolean forSet) {
    if (type instanceof EnumType) {
      if (forSet) {
        throw new RuntimeException("Can't assign to enum literal.");
      }
      EnumType enumType = (EnumType) type;
      resolvedConstant = enumType.getLiteral(pathName);
      resolvedKind = ResolvedKind.ENUM_LITERAL;
      wasm.objConst(resolvedConstant);
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
      if (forSet) {
        if (!resolvedProperty.isMutable()) {
          throw new RuntimeException("Not mutable.");
        }
        wasm.callWithContext(context -> {
          resolvedProperty.setStaticValue(context.dataStack.popObject());
        });
      } else {
        wasm.callWithContext(context -> {
          context.dataStack.pushObject(resolvedProperty.getStaticValue());
        });
      }
      return resolvedProperty.getType();
    }
    throw new RuntimeException("Classifier expected as path base; got: " + type);
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    children[0].toString(asb, errors, preferAscii);
    appendLinked(asb, "." + pathName, errors);
  }



  @Override
  public Property getResolvedProperty() {
    return resolvedProperty;
  }

}
