package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.node.Literal;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class GotoStatement extends Node {
  int resolvedTarget = -1;

  public GotoStatement(Node node) {
        super(node);
    }

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
    if (resolutionContext.mode == FunctionValidationContext.ResolutionMode.BASIC) {
      resolvedTarget = -1;
      return;
    }

    if (!(children[0] instanceof Literal) || children[0].returnType() != Types.NUMBER) {
      throw new RuntimeException("Number constant expected");
    }

    resolvedTarget = ((Number) ((Literal) children[0]).value).intValue();

    int depth = 0;
    if (resolvedTarget > line) {
      // going forward, we can't skip new variables except inside blocks.
      // we also can't skip into a block.
      // However, extra "ends" are ok
      int skippedVars = 0;
      for (Node node : resolutionContext.functionImplementation.statements(line, index, resolvedTarget, 0)) {
        if (node instanceof NextStatement || node instanceof EndIfStatement) {
          depth = depth - 1;
          if (depth < 0) {
            skippedVars = 0;
            depth = 0;
          }
        } else if (node instanceof DeclarationStatement) {
          if (depth == 0) {
            skippedVars++;
          }
        } else if (node instanceof ForStatement
            || node instanceof IfStatement && ((IfStatement) node).multiline) {
          depth++;
        }
      }
      if (skippedVars > 0) {
        throw new RuntimeException("Can't skip variable declarations.");
      }
    } else {
      for (Node node : resolutionContext.functionImplementation.descendingStatements(line, index, resolvedTarget, -1)) {
        if (node instanceof EndIfStatement || node instanceof NextStatement) {
          depth = depth + 1;
        } else if (node instanceof ForStatement || node instanceof IfStatement && ((IfStatement) node).multiline) {
           depth = depth - 1;
           if (depth < 0) {
              depth = 0;
           }
        }
      }
    }
    if (depth > 0) {
      throw new RuntimeException("Can't jump into a block.");
    }
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    evaluationContext.currentLine = resolvedTarget == -1 ? evalChildToInt(evaluationContext, 0) : resolvedTarget;
    evaluationContext.currentIndex = 0;
    return null;
  }


  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
    appendLinked(asb, "GOTO ", errors);
    children[0].toString(asb, errors);
  }

  @Override
  public Type returnType() {
        return null;
    }
}
