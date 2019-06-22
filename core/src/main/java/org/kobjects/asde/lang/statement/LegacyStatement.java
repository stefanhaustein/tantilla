package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.FunctionImplementation;
import org.kobjects.asde.lang.type.CodeLine;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.JumpStackEntry;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.node.Apply;
import org.kobjects.asde.lang.node.AssignableNode;
import org.kobjects.asde.lang.node.Identifier;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.node.RelationalOperator;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.FunctionTypeImpl;
import org.kobjects.typesystem.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;


public class LegacyStatement extends Node {

  public enum Kind {
    DATA, DEF, DUMP,
    GOSUB,
    ON,
    READ, RESTORE, RETURN,
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
  protected void onResolve(FunctionValidationContext resolutionContext, int line, int index) {
    if (resolutionContext.mode == FunctionValidationContext.ResolutionMode.STRICT) {
      throw new RuntimeException("Legacy statement " + kind + " not permitted in functions and subroutines.");
    }
  }

  public Object eval(EvaluationContext evaluationContext) {
    if (kind == null) {
      return null;
    }
    Program program = evaluationContext.control.program;
    switch (kind) {
      case DEF: {
        Node assignment = children[0];
        if (!(assignment instanceof RelationalOperator)
                || !((RelationalOperator) assignment).getName().equals("=")
                || !(assignment.children[0] instanceof Apply)
                || !(((Apply) assignment.children[0]).children[0] instanceof Identifier)) {
          throw new RuntimeException("Assignment to function declaration expected.");
        }
        Apply target = (Apply) assignment.children[0];
        String name = ((Identifier) target.children[0]).getName();
        Type[] parameterTypes = new Type[target.children.length - 1];
        String[] parameterNames = new String[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
          Identifier parameterNode = (Identifier) target.children[i + 1];
          parameterNames[i] = parameterNode.getName();
          parameterTypes[i] = parameterNode.getName().endsWith("$") ? Types.STRING : Types.NUMBER;
        }
        FunctionImplementation fn = new FunctionImplementation(program, new FunctionTypeImpl(name.endsWith("$") ? Types.STRING : Types.NUMBER, parameterTypes), parameterNames);
        fn.setLine(new CodeLine(10, new FunctionReturnStatement(assignment.children[1])));
        program.setValue(evaluationContext.getSymbolScope(), name, fn);
        fn.setDeclaringSymbol(program.getSymbol(name));
        break;
      }

      case DATA:
        break;

      case GOSUB: {
        JumpStackEntry entry = new JumpStackEntry();
        entry.lineNumber = evaluationContext.currentLine;
        entry.statementIndex = evaluationContext.currentIndex;
        evaluationContext.getJumpStack().add(entry);
        evaluationContext.currentLine = evalChildToInt(evaluationContext, 0);
        evaluationContext.currentIndex = 0;
        break;
      }

      case ON: {
        int index = (int) Math.round(evalChildToDouble(evaluationContext,0));
        if (index < children.length && index > 0) {
          if (delimiter[0].equals(" GOSUB ")) {
            JumpStackEntry entry = new JumpStackEntry();
            entry.lineNumber = evaluationContext.currentLine;
            entry.statementIndex = evaluationContext.currentIndex;
            evaluationContext.getJumpStack().add(entry);
          }
          evaluationContext.currentLine = (int) evalChildToDouble(evaluationContext, index);
          evaluationContext.currentIndex = 0;
        }
        break;
      }
      case READ:
        for (int i = 0; i < children.length; i++) {
          int[] dataPosition = evaluationContext.getDataPosition();
          while (evaluationContext.dataStatement == null
              || dataPosition[2] >= evaluationContext.dataStatement.children.length) {
            dataPosition[2] = 0;
            if (evaluationContext.dataStatement != null) {
              dataPosition[1]++;
            }
            evaluationContext.dataStatement = (LegacyStatement) program.main.find((line, index, statement)->(statement instanceof LegacyStatement && ((LegacyStatement) statement).kind == Kind.DATA), dataPosition);
            if (evaluationContext.dataStatement == null) {
              throw new RuntimeException("Out of data.");
            }
          }
          ((AssignableNode) children[i]).set(evaluationContext, evaluationContext.dataStatement.children[dataPosition[2]++].eval(evaluationContext));
        }
        break;

      case RESTORE:
        evaluationContext.dataStatement = null;
        int[] dataPosition = evaluationContext.getDataPosition();
        Arrays.fill(dataPosition, 0);
        if (children.length > 0) {
          dataPosition[0] = (int) evalChildToDouble(evaluationContext, 0);
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
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
    if (kind == null) {
      return;
    }
    appendLinked(asb, kind.name(), errors);
    if (children.length > 0) {
      appendLinked(asb, " ", errors);
      children[0].toString(asb, errors);
      for (int i = 1; i < children.length; i++) {
        asb.append((delimiter == null || i > delimiter.length) ? ", " : delimiter[i - 1]);
        children[i].toString(asb, errors);
      }
      if (delimiter != null && delimiter.length == children.length) {
        asb.append(delimiter[delimiter.length - 1]);
      }
    }
  }
}
