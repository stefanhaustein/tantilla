package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.asde.lang.ResolvedSymbol;
import org.kobjects.typesystem.Type;

import java.util.Map;

/**
 * Assignments are handled here because they might implicitly declare a transient variable -- but resolve visits the
 * children first, hitting "variable not found". An alternative might be to override resolve()
 * (in addition to onResolve) in AssignStatement.
 */
public class DeclarationStatement extends Node {
  public enum Kind {
    LET, CONST
  }

  public final String varName;
  ResolvedSymbol resolved;
  public final Kind kind;

  public DeclarationStatement(Kind kind, String varName, Node init) {
    super(init);
    this.kind = kind;
    this.varName = varName;
  }

  public void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
    resolved = resolutionContext.resolveVariableDeclaration(varName, children[0].returnType(), kind == Kind.CONST);
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    Object value = children[0].eval(evaluationContext);
    resolved.set(evaluationContext, value);
    return null;
  }

  @Override
  public Type returnType() {
    return Types.VOID;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
    appendLinked(asb, kind + " " + varName + " = ", errors);
    children[0].toString(asb, errors);
  }
}
