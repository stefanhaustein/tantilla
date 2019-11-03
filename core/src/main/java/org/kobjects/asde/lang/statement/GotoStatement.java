package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.node.Literal;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.typesystem.Type;

import java.util.Map;
import java.util.TreeMap;

public class GotoStatement extends Node {
  int target;

  public GotoStatement(Node node) {
    super();
    if (!(node instanceof Literal) || node.returnType() != Types.NUMBER) {
      throw new RuntimeException("Number constant expected");
    }
    target = ((Number) ((Literal) node).value).intValue();
  }

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
    if (resolutionContext.mode == FunctionValidationContext.ResolutionMode.LEGACY) {
      return;
    }

    int depth = 0;
    if (target > line) {
      // going forward, we can't skip new variables except inside blocks.
      // we also can't skip into a block.
      // However, extra "ends" are ok
      int skippedVars = 0;
      for (Node node : resolutionContext.functionImplementation.statements(line, index, target, 0)) {
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
      for (Node node : resolutionContext.functionImplementation.descendingStatements(line, index, target, -1)) {
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
    evaluationContext.currentLine = target;
    evaluationContext.currentIndex = 0;
    return null;
  }


  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, "GOTO " + target, errors);
  }

  @Override
  public Type returnType() {
        return null;
    }

  @Override
  public void renumber(TreeMap<Integer, Integer> renumbered) {
    Map.Entry<Integer, Integer> entry = renumbered.ceilingEntry(target);
    if (entry != null) {
      target = entry.getValue();
    }
  }
}
