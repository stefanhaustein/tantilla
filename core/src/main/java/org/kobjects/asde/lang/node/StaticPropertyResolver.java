package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.function.Callable;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;

public class StaticPropertyResolver {

  static Type resolveStaticProperty(WasmExpressionBuilder wasm, ValidationContext resolutionContext, Property resolvedProperty, boolean forSet) {
    resolutionContext.validateProperty(resolvedProperty);
    if (resolvedProperty.getType() == null) {
      throw new RuntimeException("Type of property '" + resolvedProperty + "' is null.");
    }

    if (resolvedProperty.isInstanceField()) {
      throw new RuntimeException("Static property expected for static reference.");
    }

    if (resolvedProperty.getType() instanceof FunctionType) {
      FunctionType functionType = (FunctionType) resolvedProperty.getType();
      if (functionType.getParameterCount() != 0) {
        throw new RuntimeException("Function can't be called implicitly because it has parameters.");
      }
      if (forSet) {
        throw new RuntimeException("Function can't be assigned.");
      }
      wasm.callWithContext(context -> {
        context.call((Callable) resolvedProperty.getStaticValue(), 0);
      });
      return functionType.getReturnType();
    }

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
}
