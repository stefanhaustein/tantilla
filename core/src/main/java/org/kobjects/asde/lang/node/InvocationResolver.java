package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.function.Parameter;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;

public class InvocationResolver {

  static int resolveWasm(WasmExpressionBuilder wasm, FunctionType signature, ExpressionNode[] children, int offset, boolean permitPositional, ValidationContext context, int line) {
    if (signature.getParameterCount() < children.length - offset) {
      throw new RuntimeException("Too many parameters. Expected " + signature.getParameterCount() + " but got " + children.length);
    }

    WasmExpressionBuilder[] result = new WasmExpressionBuilder[signature.getParameterCount()];
    for (int i = 0; i < children.length - offset; i++) {
      ExpressionNode value = children[i + offset];
      int parameterIndex;
      if (value instanceof Named) {
        permitPositional = false;
        Named named = (Named) value;
        parameterIndex = signature.getParameterIndex(named.name);
        if (parameterIndex == -1) {
          throw new RuntimeException("Parmeter name '" + named.name + "' not found.");
        }
        if (result[parameterIndex] != null) {
          throw new RuntimeException("Parameter '" + named.name + "' assigned already.");
        }
        value = value.children[0];
      } else if (!permitPositional) {
        throw new RuntimeException(i == 0 ? "Positional parameters are not permitted." : "Can't have positional parameters after named parameters");
      } else {
        parameterIndex = i;
      }
      Parameter parameter = signature.getParameter(parameterIndex);
      WasmExpressionBuilder childBuilder = wasm.childBuilder();
      Type actualType = value.resolveWasm(childBuilder, context, line);
      TraitCast.autoCastWasm(childBuilder, actualType, parameter.getExplicitType(), context);
      result[parameterIndex] = childBuilder;
    }

    for (int i = 0; i < result.length; i++) {
      if (result[i] == null) {
        Parameter parameter = signature.getParameter(i);
        ExpressionNode defaultValueExpression = parameter.getDefaultValueExpression();
        if (defaultValueExpression == null) {
          throw new RuntimeException("No default value available for parameter " + parameter.getName());
        }
        defaultValueExpression.resolveWasm(wasm, context, line);
      } else {
        wasm.integrateChildBuilder(result[i]);
      }
    }
    return result.length;
  }

}
