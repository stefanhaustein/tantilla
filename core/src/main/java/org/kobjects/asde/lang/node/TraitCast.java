package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.classifier.trait.AdapterInstance;
import org.kobjects.asde.lang.classifier.trait.AdapterType;
import org.kobjects.asde.lang.classifier.clazz.ClassInstance;
import org.kobjects.asde.lang.classifier.clazz.ClassType;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.classifier.trait.Trait;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.wasm.Wasm;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;
import org.kobjects.asde.lang.wasm.runtime.CallWithContext;

public class TraitCast{

  public static AdapterType getAdapterType(Type actualType, Type expectedType, ValidationContext validationContext) {
    if (expectedType.equals(actualType)) {
      return null;
    }

    if (actualType instanceof ClassType && expectedType instanceof Trait) {
      String adapterName = actualType + " as " + expectedType;
      Property property = validationContext.program.mainModule.getProperty(adapterName);
      if (property == null) {
        throw new RuntimeException("Implementation of " + adapterName + " not found.");
      }
      AdapterType adapterType = (AdapterType) property.getStaticValue();
      validationContext.addInstanceDependency(adapterType);
      return adapterType;
    }
    throw new RuntimeException("Cannot assign value of type " + actualType + " to expected type " + expectedType);

  }


  public static void autoCastWasm(WasmExpressionBuilder wasm, Type actualType, Type expectedType, ValidationContext validationContext) {
    AdapterType adapterType = getAdapterType(actualType, expectedType, validationContext);
    if (adapterType != null) {
      wasm.callWithContext(context -> {
          context.dataStack.pushObject(new AdapterInstance(adapterType, (ClassInstance) context.dataStack.popObject()));
      });
    }
  }

}
