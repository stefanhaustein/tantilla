package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.function.CodeLine;
import org.kobjects.asde.lang.function.FunctionImplementation;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.asde.lang.function.StatementSearch;
import org.kobjects.asde.lang.function.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.symbol.ResolvedSymbol;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class ForStatement extends BlockStatement {
  private final String variableName;
  ResolvedSymbol resolvedVariable;
  int resolvedNextLine;
  int resolvedNextIndex;
  int resolvedForLine;
  int resolvedForIndex;

  public ForStatement(String varName, Node... children) {
    super(children);
    this.variableName = varName;
  }

  public void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
    resolutionContext.startBlock(this, line, index);
    resolvedForLine = line;
    resolvedForIndex = index;
    // TODO: Check types?
    resolvedVariable = resolutionContext.resolveVariableDeclaration(variableName, Types.NUMBER, false);
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    double current = children[0].evalDouble(evaluationContext);
    resolvedVariable.set(evaluationContext, current);
    double end = children[1].evalDouble(evaluationContext);
    if (Math.signum(evalStep(evaluationContext)) == Math.signum(Double.compare(current, end))) {
      evaluationContext.currentLine = resolvedNextLine;
      evaluationContext.currentIndex = resolvedNextIndex + 1;
    }
    return null;
  }

  double evalStep(EvaluationContext evaluationContext) {
    return children.length > 2 ? children[2].evalDouble(evaluationContext) : 1.0;
  }


  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, "for " + variableName + " = ", errors);
    children[0].toString(asb, errors, preferAscii);
    asb.append(" to ");
    children[1].toString(asb, errors, preferAscii);
    if (children.length > 2) {
      asb.append(" step ");
      children[2].toString(asb, errors, preferAscii);
    }
  }

  @Override
  public void onResolveEnd(FunctionValidationContext resolutionContext, Node endStatement, int line, int index) {
    this.resolvedNextLine = line;
    this.resolvedNextIndex = index;
  }

  @Override
  void evalEnd(EvaluationContext evaluationContext) {
    double step = evalStep(evaluationContext);
    double current = ((Double) resolvedVariable.get(evaluationContext)) + step;
    resolvedVariable.set(evaluationContext, current);
    if (Math.signum(step) != Math.signum(Double.compare(current, children[1].evalDouble(evaluationContext)))) {
      evaluationContext.currentLine = resolvedForLine;
      evaluationContext.currentIndex = resolvedForIndex + 1;
    }
  }


}
