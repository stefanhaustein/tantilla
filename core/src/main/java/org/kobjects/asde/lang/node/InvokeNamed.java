package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.classifier.clazz.InstantiableClassType;
import org.kobjects.asde.lang.function.Callable;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.MetaType;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.type.Types;

import java.util.Map;

/**
 * Theoretically, this could be merged with apply using a "dot syntax" flag, but
 */
public class InvokeNamed extends Node {

  enum Kind {
    INSTANCE_METHOD,
    STATIC_METHOD,
    ROOT_METHOD,
    CONSTRUCTOR
  }

  public String name;
  public boolean mainModule;
  Property resolvedProperty;
  Node[] resolvedArguments;
  Kind resolvedKind;

  public InvokeNamed(String name, boolean mainModule, Node... children) {
    super(children);
    this.mainModule = mainModule;
    this.name = name;
  }

  @Override
  protected void onResolve(ValidationContext resolutionContext, int line) {
    Type baseType = mainModule ? new MetaType(resolutionContext.program.mainModule) : children[0].returnType();

    if (baseType instanceof MetaType && ((MetaType) baseType).getWrapped() instanceof Classifier) {
      resolvedProperty = ((Classifier) ((MetaType) baseType).getWrapped()).getProperty(name);
      if (resolvedProperty == null) {
        throw new RuntimeException("Property '" + name + "' not found in " + children[0].returnType());
      }
      if (resolvedProperty.getType() instanceof FunctionType) {
        resolvedKind = mainModule ? Kind.ROOT_METHOD : Kind.STATIC_METHOD;
      } else if (resolvedProperty.getType() instanceof MetaType && ((MetaType) resolvedProperty.getType()).getWrapped() instanceof InstantiableClassType) {
        InstantiableClassType instantiable = (InstantiableClassType) ((MetaType) resolvedProperty.getType()).getWrapped();
        resolutionContext.addInstanceDependency(instantiable);
        resolvedArguments = InvocationResolver.resolve(instantiable.getConstructorSignature(), children, 0, false, resolutionContext);
        resolvedKind = Kind.CONSTRUCTOR;
      } else {
        throw new RuntimeException("Can't invoke " + name);
      }
    } else if (baseType instanceof Classifier) {
      resolvedProperty = ((Classifier) baseType).getProperty(name);
      if (resolvedProperty == null) {
        throw new RuntimeException("Property '" + name + "' not found in " + children[0].returnType());
      }
      if (!(resolvedProperty.getType() instanceof FunctionType)) {
        throw new RuntimeException("Type of property '" + resolvedProperty + "' is not callable.");
      }
      resolvedKind = Kind.INSTANCE_METHOD;
    } else {
      throw new RuntimeException("Classifier or instance base expected");
    }

    resolutionContext.validateProperty(resolvedProperty);

    if (resolvedKind != Kind.CONSTRUCTOR) {
      FunctionType functionType = (FunctionType) resolvedProperty.getType();
      resolvedArguments = InvocationResolver.resolve(functionType, children, resolvedKind == Kind.STATIC_METHOD ? 1 : 0, true, resolutionContext);
    }
  }

  public Object eval(EvaluationContext evaluationContext) {
    if (resolvedKind == Kind.CONSTRUCTOR) {
      Object[] values = new Object[resolvedArguments.length];
      for (int i = 0; i < values.length; i++) {
        values[i] = resolvedArguments[i].eval(evaluationContext);
      }
      return ((InstantiableClassType) returnType()).createInstance(evaluationContext, values);
    }

    Callable function = (Callable) resolvedProperty.getStaticValue();
      evaluationContext.ensureExtraStackSpace(function.getLocalVariableCount());
      for (int i = 0; i < resolvedArguments.length; i++) {
        evaluationContext.push(resolvedArguments[i].eval(evaluationContext));
      }

     evaluationContext.popN(resolvedArguments.length);
     try {
       Object result = function.call(evaluationContext, resolvedArguments.length);
       if (result == null && returnType() != Types.VOID) {
         throw new NullPointerException("non-void method evaluation of " + function + " returns null");
       }
       return result;
     } catch (Exception e) {
       throw new RuntimeException(e.getMessage() + " in " + this, e);
     }
  }

  // Shouldn't throw, as it's used outside validation!
  public Type returnType() {
    if (resolvedProperty == null) {
      return null;
    }
    if (resolvedKind == Kind.CONSTRUCTOR) {
      return ((MetaType) resolvedProperty.getType()).getWrapped();
    }
    FunctionType functionType = (FunctionType) resolvedProperty.getType();
    return functionType == null ? null : functionType.getReturnType();
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    int start = asb.length();
    int startIndex = 0;
    if (!mainModule) {
      children[startIndex++].toString(asb, errors, preferAscii);
      asb.append(".");
    }
    appendLinked(asb, name, errors);
    asb.append("(");
    for (int i = startIndex; i < children.length; i++) {
      if (i > startIndex) {
        asb.append(", ");
      }
      children[i].toString(asb, errors, preferAscii);
    }
    asb.append(")");
    asb.annotate(start, asb.length(), errors.get(this));
  }


  @Override
  public void reorderParameters(Property symbol, int[] oldIndices) {
    if (resolvedProperty != symbol) {
      return;
    }

    Node[] oldChildren = children;
    children = new Node[oldIndices.length];
    for (int i = 0; i < oldIndices.length; i++) {
      if (oldIndices[i] != -1) {
        children[i] = oldChildren[oldIndices[i] + (mainModule ? 0 : 1)];
      } else {
        children[i] = new Identifier("placeholder" + i);
      }
    }
  }

}
