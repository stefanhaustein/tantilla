package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.asde.lang.symbol.ResolvedSymbol;
import org.kobjects.asde.lang.symbol.StaticSymbol;
import org.kobjects.asde.lang.array.ArrayType;
import org.kobjects.asde.lang.function.Types;
import org.kobjects.typesystem.FunctionTypeImpl;
import org.kobjects.typesystem.Type;

import java.util.Map;

// Not static for access to the variables.
public class Identifier extends SymbolNode {
  String name;
  ResolvedSymbol resolved;

  public Identifier(String name) {
    this.name = name;
  }

  public void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
    Type impliedType;
    if (resolutionContext.mode != FunctionValidationContext.ResolutionMode.LEGACY) {
      impliedType = null;
    } else {
      impliedType = name.endsWith("$") ? Types.STRING : Types.NUMBER;
      if (parent instanceof Apply && parent.children[0] == this) {
        if (name.toLowerCase().startsWith("fn")) {
          Type[] parameterTypes = new Type[parent.children.length - 1];
          for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypes[i] = parent.children[i + 1].returnType();
          }
          impliedType = new FunctionTypeImpl(impliedType, parameterTypes);
        } else if (parent.children.length > 1) {
          impliedType = new ArrayType(impliedType, parent.children.length - 1);
        }
      }
    }
    resolved = resolutionContext.resolveVariableAccess(name, impliedType);
  }

  @Override
  public void resolveForAssignment(FunctionValidationContext resolutionContext, Node parent, Type type, int line, int index) {
    resolved = resolutionContext.resolveVariableAssignment(name, type);
  }

  public void set(EvaluationContext evaluationContext, Object value) {
    resolved.set(evaluationContext, value);
  }

  @Override
  public boolean isConstant() {
    return resolved.isConstant();
  }

  @Override
  public boolean isAssignable() {
    return !isConstant();
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    return resolved.get(evaluationContext);
  }

  public Type returnType() {
    return  resolved.getType();
  }

  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, name, errors);
  }


  public String getName() {
    return name;
  }


  @Override
  public void rename(StaticSymbol symbol, String oldName, String newName) {
    if (matches(symbol, oldName)) {
      this.name = newName;
    }
  }

  @Override
  public boolean matches(StaticSymbol symbol, String name) {
    return resolved instanceof StaticSymbol && this.name.equals(name);
  }

}
