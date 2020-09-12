package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.function.LocalSymbol;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.wasm.Wasm;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;
import org.kobjects.markdown.AnnotatedStringBuilder;

import java.util.Map;

// Not static for access to the variables.
public class Identifier extends AssignableWasmNode implements HasProperty {


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

  @Override
  protected Type resolveWasmImpl(WasmExpressionBuilder wasm, ValidationContext resolutionContext, int line) {
    return resolveWasmImpl(wasm, resolutionContext, line, /* forSet= */ false);
  }

  @Override
  public Type resolveForAssignment(WasmExpressionBuilder wasm, ValidationContext validationContext, int line) {
    return resolveWasmImpl(wasm, validationContext, line, /* forSet */ true);
  }


  private Type resolveWasmImpl(WasmExpressionBuilder wasm, ValidationContext resolutionContext, int line, boolean forSet) {
    resolvedKind = Kind.ERROR;
    resolvedRootProperty = null;
    resolvedLocalVariable = resolutionContext.getCurrentBlock().get(name);

    if (resolvedLocalVariable != null) {
      resolvedMutable = resolvedLocalVariable.isMutable();
      resolvedKind = Kind.LOCAL_VARIABLE;
      wasm.opCode(forSet ? Wasm.LOCAL_SET : Wasm.LOCAL_GET);
      wasm.integer(resolvedLocalVariable.index);
      resolvedType = resolvedLocalVariable.getType();
    } else {
      resolvedRootProperty = resolutionContext.program.mainModule.getProperty(name);
      if (resolvedRootProperty == null) {
        throw new RuntimeException("Variable not found: '" + name + "'");
      }
      resolvedKind = Kind.ROOT_MODULE_PROPERTY;
      resolvedType = StaticPropertyResolver.resolveStaticProperty(wasm, resolutionContext, resolvedRootProperty, forSet);
    }
    return resolvedType;
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
