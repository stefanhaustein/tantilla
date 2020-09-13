package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.classifier.clazz.InstantiableClassType;
import org.kobjects.asde.lang.function.Callable;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.MetaType;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;
import org.kobjects.markdown.AnnotatedStringBuilder;

import java.util.Map;

public class Invoke extends WasmNode {

  final boolean parenthesis;
  Property resolvedProperty;

  public Invoke(boolean parenthesis, Node... children) {
    super(children);
    this.parenthesis = parenthesis;
  }

  @Override
  protected Type resolveWasmImpl(WasmExpressionBuilder wasm, ValidationContext resolutionContext, int line) {

    // Special cases for paths and identifiers to avoid implict invocation

    Node base = children[0];

    if (base instanceof Path) {
      return resolvePathInvocation(wasm, resolutionContext, line, base, ((Path) base).pathName);
    }
    if (base instanceof Identifier) {
      return resolveStaticInvocation(wasm, resolutionContext, line, resolutionContext.program.mainModule, ((Identifier) base).name);
    }

    Type baseType = children[0].resolveWasm(wasm, resolutionContext, line);
    if (!(baseType instanceof FunctionType)) {
      throw new RuntimeException("Base type needs to be function or constructor.");
    }
    FunctionType functionType = (FunctionType) baseType;

    final int count = InvocationResolver.resolveWasm(wasm,functionType, children, 1, true, resolutionContext, line);

    wasm.callWithContext(context -> {
      Callable function = (Callable) context.dataStack.getObject(context.dataStack.size() - count - 1);
      Object result = context.call(function, count);
      context.dataStack.setObject(context.dataStack.size() - 1, result);
    });

    return functionType.getReturnType();
  }

  private Type resolvePathInvocation(WasmExpressionBuilder wasm, ValidationContext resolutionContext, int line, Node path, String name) {
    Type baseType = path.children[0].resolveWasm(new WasmExpressionBuilder(), resolutionContext, line);

    // Static
    if (baseType instanceof MetaType && ((MetaType) baseType).getWrapped() instanceof Classifier) {
      return resolveStaticInvocation(wasm, resolutionContext, line, (Classifier) ((MetaType) baseType).getWrapped(), name);
    }

    if (baseType instanceof Classifier) {
      Node[] adjustedChildren = new Node[children.length];
      adjustedChildren[0] = path.children[0];
      for (int i = 1; i < adjustedChildren.length; i++) {
        adjustedChildren[i] = children[i];
      }

      resolvedProperty = ((Classifier) baseType).getProperty(name);
      if (resolvedProperty == null) {
        throw new RuntimeException("Property '" + name + "' not found in " + adjustedChildren[0].returnType());
      }

      resolutionContext.validateProperty(resolvedProperty);

      if (!(resolvedProperty.getType() instanceof FunctionType)) {
        throw new RuntimeException("Type of property '" + resolvedProperty + "' is not callable.");
      }
      
      FunctionType functionType = (FunctionType) resolvedProperty.getType();
      final int count = InvocationResolver.resolveWasm(wasm, functionType, adjustedChildren, 0, true, resolutionContext, line);
      wasm.callWithContext(context -> {
          Callable function = (Callable) resolvedProperty.getStaticValue();
          context.dataStack.pushObject(context.call(function, count));
      });
      return functionType.getReturnType();
    }

    throw new RuntimeException("Classifier or instance base expected");
  }


  /**
   * Ignores the first parameter.
   */
  private Type resolveStaticInvocation(WasmExpressionBuilder wasm, ValidationContext resolutionContext, int line, Classifier classifier, String name) {
    resolvedProperty = classifier.getProperty(name);
    if (resolvedProperty == null) {
      throw new RuntimeException("Property '" + name + "' not found in " + classifier);
    }

    resolutionContext.validateProperty(resolvedProperty);

    if (resolvedProperty.getType() instanceof FunctionType) {
      FunctionType functionType = (FunctionType) resolvedProperty.getType();
      final int count = InvocationResolver.resolveWasm(wasm, functionType, children, 1, true, resolutionContext, line);
      wasm.callWithContext(context -> {
        Callable function = (Callable) resolvedProperty.getStaticValue();
        context.dataStack.pushObject(context.call(function, count));
      });
      return functionType.getReturnType();
    }

    if (resolvedProperty.getType() instanceof MetaType
        && ((MetaType) resolvedProperty.getType()).getWrapped() instanceof InstantiableClassType) {
      final InstantiableClassType instantiable = (InstantiableClassType) ((MetaType) resolvedProperty.getType()).getWrapped();
      resolutionContext.addInstanceDependency(instantiable);
      final int count = InvocationResolver.resolveWasm(wasm, instantiable.getConstructorSignature(), children, 1, false, resolutionContext, line);
      wasm.callWithContext(context -> {
        Object[] args = new Object[count];
        for (int i = 0; i < count; i++) {
          args[count - i - 1] = context.dataStack.popObject();
        }
        context.dataStack.pushObject(instantiable.createInstance(context, args));
      });
      return instantiable;
    }

    throw new RuntimeException("Can't invoke " + name);
  }


  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    int start = asb.length();
    int startIndex = 0;

    children[startIndex++].toString(asb, errors, preferAscii);

    asb.append(parenthesis ? '(' : ' ');
    for (int i = startIndex; i < children.length; i++) {
      if (i > startIndex) {
        asb.append(", ");
      }
      children[i].toString(asb, errors, preferAscii);
    }
    if (parenthesis) {
      asb.append(')');
    }
    asb.annotate(start, asb.length(), errors.get(this));
  }

}
