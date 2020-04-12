package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.function.LocalSymbol;
import org.kobjects.asde.lang.io.SyntaxColor;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;


public class DeclarationStatement extends Statement {

  public enum Kind {
    MUT, LET
  }

  public final Kind kind;
  String varName;
  LocalSymbol resolved;

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    resolved.set(evaluationContext, evalValue(evaluationContext));
    return null;
  }

  public String getVarName() {
    return varName;
  }

  public void setVarName(String newName) {
    this.varName = varName;
  }

  public DeclarationStatement(Kind kind, String varName, Node init) {
    super(init);
    this.varName = varName;
    this.kind = kind;
  }

  public void onResolve(ValidationContext resolutionContext, int line) {
    resolved = resolutionContext.declareLocalVariable(varName, children[0].returnType(), kind != Kind.LET);
  }

  public Object evalValue(EvaluationContext evaluationContext) {
    return children[0].eval(evaluationContext);
  }


  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    asb.append(kind.name().toLowerCase(), SyntaxColor.KEYWORD);
    appendLinked(asb, " " + varName + " = ", errors);
    children[0].toString(asb, errors, preferAscii);
  }
}
