package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Array;
import org.kobjects.asde.lang.CallableUnit;
import org.kobjects.asde.lang.CodeLine;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.StackEntry;
import org.kobjects.asde.lang.Types;
import org.kobjects.asde.lang.node.Apply;
import org.kobjects.asde.lang.node.AssignableNode;
import org.kobjects.asde.lang.node.Identifier;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.node.Operator;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Type;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;


public class SimpleStatement extends Node {

  public enum Kind {
    DATA, DIM, DEF, DUMP,
    END,
    GOTO, GOSUB,
    INPUT,
    ON,
    PRINT,
    READ, REM, RESTORE, RETURN,
     STOP,
    PAUSE,
  }

  final Program program;
  public final Kind kind;
  final String[] delimiter;

  public SimpleStatement(Program program, Kind kind, String[] delimiter, Node... children) {
    super(children);
    this.program = program;
    this.kind = kind;
    this.delimiter = delimiter;
  }

  public SimpleStatement(Program program, Kind kind, Node... children) {
    this(program, kind, null, children);
  }

  public Object eval(Interpreter interpreter) {
    if (kind == null) {
      return null;
    }

    if (program.trace) {
      program.print(interpreter.currentLine + ":" + interpreter.currentIndex + ": " + this);
    }

    switch (kind) {

      case DEF: {
        Node assignment = children[0];
        if (!(assignment instanceof Operator)
                || !((Operator) assignment).name.equals("=")
                || !(assignment.children[0] instanceof Apply)
                || !(((Apply) assignment.children[0]).children[0] instanceof Identifier)) {
          throw new RuntimeException("Assignment to function declaration expected.");
        }
        Apply target = (Apply) assignment.children[0];
        String name = ((Identifier) target.children[0]).name;
        Type[] parameterTypes = new Type[target.children.length - 1];
        String[] parameterNames = new String[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
          Identifier parameterNode = (Identifier) target.children[i + 1];
          parameterNames[i] = parameterNode.name;
          parameterTypes[i] = parameterNode.name.endsWith("$") ? Types.STRING : Types.NUMBER;
        }
        CallableUnit fn = new CallableUnit(program, new FunctionType(name.endsWith("$") ? Types.STRING : Types.NUMBER, parameterTypes), parameterNames);
        fn.setLine(10, new CodeLine(Collections.singletonList(new SimpleStatement(program, Kind.RETURN, assignment.children[1]))));
        program.setValue(interpreter.getSymbolScope(), name, fn);
        break;
      }
      case DATA:
      case REM:
        break;

      case DIM: {
        for (Node expr : children) {
          if (!(expr instanceof Apply)) {
            throw new RuntimeException("DIM Syntax error");
          }
          if (!(expr.children[0] instanceof Identifier)) {
            throw new RuntimeException("DIM identifier expected");
          }
          String name = ((Identifier) expr.children[0]).name;
          int[] dims = new int[expr.children.length - 1];
          for (int i = 0; i < dims.length; i++) {
            // TODO: evalInt
            dims[i] = ((Number) expr.children[i + 1].eval(interpreter)).intValue();
          }
          program.setValue(interpreter.getSymbolScope(), name, new Array(name.endsWith("$") ? Types.STRING : Types.NUMBER, dims));
        }
        break;
      }

      case END:
        interpreter.currentLine = Integer.MAX_VALUE;
        interpreter.currentIndex = 0;
        break;

      case GOSUB: {
        StackEntry entry = new StackEntry();
        entry.lineNumber = interpreter.currentLine;
        entry.statementIndex = interpreter.currentIndex;
        interpreter.stack.add(entry);
      }  // Fallthrough intended
      case GOTO:
        interpreter.currentLine = (int) evalDouble(interpreter,0);
        interpreter.currentIndex = 0;
        break;

      case INPUT:
        input(interpreter);
        break;

      case PAUSE:
        try {
          Thread.sleep(evalInt(interpreter, 0));
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
        break;

      case PRINT:
        for (int i = 0; i < children.length; i++) {
          Object val = children[i].eval(interpreter);
          if (val instanceof Double) {
            double d = (Double) val;
            program.print((d < 0 ? "" : " ") + Program.toString(d) + " ");
          } else {
            program.print(Program.toString(val));
          }
          if (i < delimiter.length && delimiter[i].equals(", ")) {
            program.print(
                "                    ".substring(0, 14 - (program.tabPos % 14)));
          }
        }
        if (delimiter.length < children.length &&
            (children.length == 0 || !children[children.length - 1].toString().startsWith("TAB"))) {
          program.print("\n");
        }
        break;

      case ON: {
        int index = (int) Math.round(evalDouble(interpreter,0));
        if (index < children.length && index > 0) {
          if (delimiter[0].equals(" GOSUB ")) {
            StackEntry entry = new StackEntry();
            entry.lineNumber = interpreter.currentLine;
            entry.statementIndex = interpreter.currentIndex;
            interpreter.stack.add(entry);
          }
          interpreter.currentLine = (int) evalDouble(interpreter, index);
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
            interpreter.dataStatement = (SimpleStatement) program.main.find((Node statement)->(statement instanceof SimpleStatement && ((SimpleStatement) statement).kind == Kind.DATA), interpreter.dataPosition);
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
          interpreter.dataPosition[0] = (int) evalDouble(interpreter, 0);
        }
        break;

      case RETURN:
        if (children.length > 0) {
          interpreter.returnValue = children[0].eval(interpreter);
          interpreter.currentLine = Integer.MAX_VALUE;
          interpreter.currentIndex = 0;
        } else {
          while (true) {
            if (interpreter.stack.isEmpty()) {
              throw new RuntimeException("RETURN without GOSUB.");
            }
            StackEntry entry = interpreter.stack.remove(interpreter.stack.size() - 1);
            if (entry.forVariable == null) {
              interpreter.currentLine = entry.lineNumber;
              interpreter.currentIndex = entry.statementIndex + 1;
              break;
            }
          }
        }
        break;
      case STOP:
        program.stopped = new int[]{interpreter.currentLine, interpreter.currentIndex};
        program.println("\nSTOPPED in " + interpreter.currentLine + ":" + interpreter.currentIndex);
        interpreter.currentLine = Integer.MAX_VALUE;
        interpreter.currentIndex = 0;
        break;

      default:
        throw new RuntimeException("Unimplemented statement: " + kind);
    }
    if (program.trace) {
      program.println();
    }
    return null;
  }




  void input(Interpreter interpreter) {
    for (int i = 0; i < children.length; i++) {
      Node child = children[i];
      if (kind == Kind.INPUT && child instanceof Identifier) {
        if (i <= 0 || i > delimiter.length || !delimiter[i-1].equals(", ")) {
          program.print("? ");
        }
        Identifier variable = (Identifier) child;
        Object value;
        while(true) {
          value = program.console.read();
          if (variable.name.endsWith("$")) {
            break;
          }
          try {
            value = Double.parseDouble((String) value);
            break;
          } catch (NumberFormatException e) {
            program.print("Not a number. Please enter a number: ");
          }
        }
        variable.set(interpreter, value);
      } else {
        program.print(Program.toString(child.eval(interpreter)));
      }
    }
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
