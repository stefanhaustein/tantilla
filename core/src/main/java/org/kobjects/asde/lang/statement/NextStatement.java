package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class NextStatement extends Node {

  String varName;

  public ForStatement resolvedForStatement;
  public int resolvedForLine;
  public int resolvedForIndex;

  public NextStatement(String varName) {
        this.varName = varName;
    }

    @Override
    public Object eval(EvaluationContext evaluationContext) {
        double step = resolvedForStatement.evalStep(evaluationContext);
        double current = ((Double) resolvedForStatement.resolvedVariable.get(evaluationContext)) + step;
        resolvedForStatement.resolvedVariable.set(evaluationContext, current);
      if (Math.signum(step) != Math.signum(Double.compare(current, resolvedForStatement.children[1].evalDouble(evaluationContext)))) {
            evaluationContext.currentLine = resolvedForLine;
            evaluationContext.currentIndex = resolvedForIndex + 1;
        }
        return null;
    }

    @Override
    public Type returnType() {
        return Types.VOID;
    }

    public void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
        if (resolvedForStatement == null) {
          throw new RuntimeException("NEXT without FOR");
        }
        resolutionContext.endBlock(FunctionValidationContext.BlockType.FOR);
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
        if (varName == null) {
            appendLinked(asb, "NEXT", errors);
        } else {
            appendLinked(asb, "NEXT " + varName, errors);
        }
    }
}
