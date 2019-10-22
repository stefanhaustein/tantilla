package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.asde.lang.ResolvedSymbol;
import org.kobjects.asde.lang.StaticSymbol;
import org.kobjects.asde.lang.type.ArrayType;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.FunctionTypeImpl;
import org.kobjects.typesystem.Type;

import java.util.Map;

// Not static for access to the variables.
public class Identifier extends SymbolNode {
  final Program program;
  String name;
  ResolvedSymbol resolved;

  public Identifier(Program program, String name) {
    this.program = program;
    this.name = name;
  }

  public void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
    Type impliedType;
    if (resolutionContext.mode != FunctionValidationContext.ResolutionMode.BASIC) {
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

  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
    appendLinked(asb, name, errors);
  }

  public void accept(Visitor visitor) {
    visitor.visitIdentifier(this);
  }

  public String getName() {
    return name;
  }

  public ResolvedSymbol getResolved() {
    return resolved;
  }

  @Override
  public boolean matches(StaticSymbol symbol, String oldName) {
    return symbol instanceof StaticSymbol && name.equals(oldName);
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }
}
