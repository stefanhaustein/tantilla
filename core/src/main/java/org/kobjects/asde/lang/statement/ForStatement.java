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

public class ForStatement extends Statement {
  private final String variableName;
  ResolvedSymbol resolvedVariable;
  int resolvedNextLine;
  int resolvedNextIndex;

  public ForStatement(String varName, Node... children) {
    super(children);
    this.variableName = varName;
  }

  public void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
    resolutionContext.startBlock(FunctionValidationContext.BlockType.FOR);
    // TODO: Check types?
    resolvedVariable = resolutionContext.resolveVariableDeclaration(variableName, Types.NUMBER, false);

    NextSearch search = new NextSearch(resolutionContext.functionImplementation);
    NextStatement nextStatement = (NextStatement) search.find(line, index);
    if (nextStatement == null) {
      throw new RuntimeException("FOR without NEXT");
    }

    resolvedNextLine = search.lineNumber;
    resolvedNextIndex = search.index;

    nextStatement.resolvedForStatement = this;
    nextStatement.resolvedForLine = line;
    nextStatement.resolvedForIndex = index;
    if (nextStatement.varName == null) {
      nextStatement.varName = variableName;
    }
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
    appendLinked(asb, "FOR " + variableName + " = ", errors);
    children[0].toString(asb, errors, preferAscii);
    asb.append(" TO ");
    children[1].toString(asb, errors, preferAscii);
    if (children.length > 2) {
      asb.append(" STEP ");
      children[2].toString(asb, errors, preferAscii);
    }
  }


  class NextSearch extends StatementSearch {

    NextSearch(FunctionImplementation functionImplementation) {
      super(functionImplementation);
    }

    @Override
    public boolean statementMatches(CodeLine line, int index, Node statement) {
      return statement instanceof NextStatement
          && (variableName.equals(((NextStatement) statement).varName) || ((NextStatement) statement).varName == null);
    }
  }
}
