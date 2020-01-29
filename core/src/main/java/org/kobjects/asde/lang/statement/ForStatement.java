package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.array.Array;
import org.kobjects.asde.lang.array.ArrayType;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.asde.lang.function.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.symbol.ResolvedSymbol;
import org.kobjects.typesystem.Type;
import org.kobjects.typesystem.TypeImpl;

import java.util.Iterator;
import java.util.Map;

public class ForStatement extends BlockStatement {
  private final String variableName;
  ResolvedSymbol resolvedVariable;
  ResolvedSymbol resolvedIterator;
  int resolvedNextLine;
  int resolvedForLine;

  static Type ITERATOR = new TypeImpl("iterator", null){};

  public ForStatement(String varName, Node child) {
    super(child);
    this.variableName = varName;
  }

  public void onResolve(FunctionValidationContext resolutionContext, Node parent, int line) {
    resolutionContext.startBlock(this, line);
    resolvedForLine = line;

    if (!(children[0].returnType() instanceof ArrayType)) {
      throw new RuntimeException("in expression must result in a list");
    }
    ArrayType arrayType = (ArrayType) children[0].returnType();
    resolvedIterator = resolutionContext.resolveVariableDeclaration(variableName + "-iterator", ITERATOR, false);
    resolvedVariable = resolutionContext.resolveVariableDeclaration(variableName, arrayType.elementType, false);
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    Iterator<Object> iterator = ((Array) children[0].eval(evaluationContext)).iterator();
    if (!iterator.hasNext()) {
      evaluationContext.currentLine = resolvedNextLine + 1;
    } else {
      resolvedIterator.set(evaluationContext, iterator);
      resolvedVariable.set(evaluationContext, iterator.next());
    }
    return null;
  }


  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, "for " + variableName + " in ", errors);
    children[0].toString(asb, errors, preferAscii);
    asb.append(':');
  }

  @Override
  public void onResolveEnd(FunctionValidationContext resolutionContext, Node endStatement, int line) {
    this.resolvedNextLine = line;
  }

  @Override
  void evalEnd(EvaluationContext evaluationContext) {
    Iterator<?> iterator = (Iterator<?>) resolvedIterator.get(evaluationContext);
    if (iterator.hasNext()) {
      resolvedVariable.set(evaluationContext, iterator.next());
    } else {
      evaluationContext.currentLine = resolvedForLine + 1;
    }
  }
}
