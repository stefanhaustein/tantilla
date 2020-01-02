package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.runtime.JumpStackEntry;
import org.kobjects.asde.lang.node.Literal;
import org.kobjects.asde.lang.function.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.typesystem.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;


public class LegacyStatement extends Node {

  public enum Kind {
    DATA, DUMP,
    GOSUB,
    ON,
    RESTORE, RETURN,
    STOP,
  }

//  final Program program;
  public final Kind kind;
  final String[] delimiter;

  public LegacyStatement(Kind kind, String[] delimiter, Node... children) {
    super(children);
  //  this.program = program;
    this.kind = kind;
    this.delimiter = delimiter;
  }

  public LegacyStatement(Kind kind, Node... children) {
    this(kind, null, children);
  }

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
    if (resolutionContext.mode == FunctionValidationContext.ResolutionMode.PROGRAM) {
      throw new RuntimeException("Legacy statement " + kind + " not permitted in functions and subroutines.");
    }
  }

  public Object eval(EvaluationContext evaluationContext) {
    if (kind == null) {
      return null;
    }
    switch (kind) {
      case DATA:
        break;

      case GOSUB: {
        JumpStackEntry entry = new JumpStackEntry();
        entry.lineNumber = evaluationContext.currentLine;
        entry.statementIndex = evaluationContext.currentIndex;
        evaluationContext.getJumpStack().add(entry);
        evaluationContext.currentLine = children[0].evalInt(evaluationContext);
        evaluationContext.currentIndex = 0;
        break;
      }

      case ON: {
        int index = (int) Math.round(children[0].evalDouble(evaluationContext));
        if (index < children.length && index > 0) {
          if (delimiter[0].equals(" GOSUB ")) {
            JumpStackEntry entry = new JumpStackEntry();
            entry.lineNumber = evaluationContext.currentLine;
            entry.statementIndex = evaluationContext.currentIndex;
            evaluationContext.getJumpStack().add(entry);
          }
          evaluationContext.currentLine = (int) children[index].evalDouble(evaluationContext);
          evaluationContext.currentIndex = 0;
        }
        break;
      }

      case RESTORE:
        evaluationContext.dataStatement = null;
        int[] dataPosition = evaluationContext.getDataPosition();
        Arrays.fill(dataPosition, 0);
        if (children.length > 0) {
          dataPosition[0] = (int) children[0].evalDouble(evaluationContext);
        }
        break;

      case RETURN: {
        ArrayList<JumpStackEntry> jumpStack = evaluationContext.getJumpStack();
        if (jumpStack.isEmpty()) {
          throw new RuntimeException("RETURN without GOSUB.");
        }
        JumpStackEntry entry = jumpStack.remove(jumpStack.size() - 1);
        evaluationContext.currentLine = entry.lineNumber;
        evaluationContext.currentIndex = entry.statementIndex + 1;
        break;
      }

      case STOP:
        evaluationContext.control.pause();
        break;

      default:
        throw new RuntimeException("Unimplemented statement: " + kind);
    }
    return null;
  }

  @Override
  public Type returnType() {
    return Types.VOID;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    if (kind == null) {
      return;
    }
    appendLinked(asb, kind.name(), errors);
    if (children.length > 0) {
      appendLinked(asb, " ", errors);
      children[0].toString(asb, errors, preferAscii);
      for (int i = 1; i < children.length; i++) {
        asb.append((delimiter == null || i > delimiter.length) ? ", " : delimiter[i - 1]);
        children[i].toString(asb, errors,preferAscii );
      }
      if (delimiter != null && delimiter.length == children.length) {
        asb.append(delimiter[delimiter.length - 1]);
      }
    }
  }


  @Override
  public void renumber(TreeMap<Integer, Integer> renumbered) {
    if (kind == Kind.GOSUB || kind == Kind.ON) {
      for (int i = 0; i < children.length; i++) {
        if (children[i] instanceof Literal) {
          Literal literal = (Literal) children[i];
          if (literal.value instanceof Number) {
            int target = ((Number) literal.value).intValue();
            Map.Entry<Integer, Integer> entry = renumbered.ceilingEntry(target);
            if (entry != null) {
              children[i] = new Literal(Double.valueOf(entry.getValue()));
            }
          }
        }
      }
    }
  }

}
