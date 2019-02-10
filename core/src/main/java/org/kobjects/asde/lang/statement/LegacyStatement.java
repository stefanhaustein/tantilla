package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.type.CallableUnit;
import org.kobjects.asde.lang.type.CodeLine;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.JumpStackEntry;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.node.Apply;
import org.kobjects.asde.lang.node.AssignableNode;
import org.kobjects.asde.lang.node.Identifier;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.node.RelationalOperator;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Type;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;


public class LegacyStatement extends Node {

  public enum Kind {
    DATA, DEF, DUMP,
    END,
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

  public Object eval(Interpreter interpreter) {
    if (kind == null) {
      return null;
    }
    Program program = interpreter.control.program;
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
        CallableUnit fn = new CallableUnit(program, new FunctionType(name.endsWith("$") ? Types.STRING : Types.NUMBER, parameterTypes), parameterNames);
        fn.setLine(10, new CodeLine(Collections.singletonList(new FunctionReturnStatement(assignment.children[1]))));
        program.setValue(interpreter.getSymbolScope(), name, fn);
        break;
      }

      case DATA:
        break;

      case END:
        interpreter.currentLine = Integer.MAX_VALUE;
        interpreter.currentIndex = 0;
        break;

      case GOSUB: {
        JumpStackEntry entry = new JumpStackEntry();
        entry.lineNumber = interpreter.currentLine;
        entry.statementIndex = interpreter.currentIndex;
        interpreter.stack.add(entry);
        interpreter.currentLine = evalChildToInt(interpreter, 0);
        interpreter.currentIndex = 0;
        break;
      }

      case ON: {
        int index = (int) Math.round(evalChildToDouble(interpreter,0));
        if (index < children.length && index > 0) {
          if (delimiter[0].equals(" GOSUB ")) {
            JumpStackEntry entry = new JumpStackEntry();
            entry.lineNumber = interpreter.currentLine;
            entry.statementIndex = interpreter.currentIndex;
            interpreter.stack.add(entry);
          }
          interpreter.currentLine = (int) evalChildToDouble(interpreter, index);
          interpreter.currentIndex = 0;
        }
        break;
      }
      case READ:
        for (int i = 0; i < children.length; i++) {
          while (interpreter.dataStatement == null
              || interpreter.dataPosition[2] >= interpreter.dataStatement.children.length) {
            interpreter.dataPosition[2] = 0;
            if (interpreter.dataStatement != null) {
              interpreter.dataPosition[1]++;
            }
            interpreter.dataStatement = (LegacyStatement) program.main.find((Node statement)->(statement instanceof LegacyStatement && ((LegacyStatement) statement).kind == Kind.DATA), interpreter.dataPosition);
            if (interpreter.dataStatement == null) {
              throw new RuntimeException("Out of data.");
            }
          }
          ((AssignableNode) children[i]).set(interpreter, interpreter.dataStatement.children[interpreter.dataPosition[2]++].eval(interpreter));
        }
        break;

      case RESTORE:
        interpreter.dataStatement = null;
        Arrays.fill(interpreter.dataPosition, 0);
        if (children.length > 0) {
          interpreter.dataPosition[0] = (int) evalChildToDouble(interpreter, 0);
        }
        break;

      case RETURN:
        while (true) {
          if (interpreter.stack.isEmpty()) {
            throw new RuntimeException("RETURN without GOSUB.");
          }
          JumpStackEntry entry = interpreter.stack.remove(interpreter.stack.size() - 1);
          if (entry.forVariable == null) {
            interpreter.currentLine = entry.lineNumber;
            interpreter.currentIndex = entry.statementIndex + 1;
            break;
          }
        }
        break;

      case STOP:
        interpreter.control.pause();
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
