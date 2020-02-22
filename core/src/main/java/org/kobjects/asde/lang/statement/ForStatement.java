package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.io.SyntaxColor;
import org.kobjects.asde.lang.list.ListImpl;
import org.kobjects.asde.lang.list.ListType;
import org.kobjects.asde.lang.function.PropertyValidationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.symbol.ResolvedSymbol;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.type.TypeImpl;

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

  public void onResolve(PropertyValidationContext resolutionContext, int line) {
    resolutionContext.startBlock(this);
    resolvedForLine = line;

    if (!(children[0].returnType() instanceof ListType)) {
      throw new RuntimeException("in expression must result in a list");
    }
    ListType arrayType = (ListType) children[0].returnType();
    resolvedIterator = resolutionContext.resolveVariableDeclaration(variableName + "-iterator", ITERATOR, false);
    resolvedVariable = resolutionContext.resolveVariableDeclaration(variableName, arrayType.elementType, false);
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    Iterator<Object> iterator = ((ListImpl) children[0].eval(evaluationContext)).iterator();
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
    appendLinked(asb, "for ", errors, SyntaxColor.KEYWORD);
    appendLinked(asb, variableName, errors);
    appendLinked(asb, " in ", errors, SyntaxColor.KEYWORD);
    children[0].toString(asb, errors, preferAscii);
    asb.append(':');
  }

  @Override
  public void onResolveEnd(PropertyValidationContext resolutionContext, EndStatement endStatement, int endLine) {
    this.resolvedNextLine = endLine;
  }

  @Override
  void evalEnd(EvaluationContext evaluationContext) {
    Iterator<?> iterator = (Iterator<?>) resolvedIterator.get(evaluationContext);
    if (iterator.hasNext()) {
      resolvedVariable.set(evaluationContext, iterator.next());
      evaluationContext.currentLine = resolvedForLine + 1;
    }
  }
}
