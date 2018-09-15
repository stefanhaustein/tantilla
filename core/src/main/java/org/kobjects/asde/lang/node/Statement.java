package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Array;
import org.kobjects.asde.lang.AsdeShell;
import org.kobjects.asde.lang.CallableUnit;
import org.kobjects.asde.lang.CodeLine;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.StackEntry;
import org.kobjects.asde.lang.Types;
import org.kobjects.asde.lang.symbol.GlobalSymbol;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Type;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class Statement extends Node {

  public enum Kind {
    CLEAR, CONTINUE,
    DATA, DIM, DEF, DUMP,
    END,
    GOTO, GOSUB,
    IF, INPUT,
    LIST, LOAD,
    NEXT,
    ON,
    PRINT,
    READ, REM, RESTORE, RETURN, RUN,
    SAVE, STOP,
    TRON, PAUSE, TROFF
  }

  final Program program;
  public final Kind kind;
  final String[] delimiter;

  public Statement(Program program, Kind kind, String[] delimiter, Node... children) {
    super(children);
    this.program = program;
    this.kind = kind;
    this.delimiter = delimiter;
  }

  public Statement(Program program, Kind kind, Node... children) {
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
      case CONTINUE:
        if (program.stopped == null) {
          throw new RuntimeException("Not stopped.");
        }
        interpreter.currentLine = program.stopped[0];
        interpreter.currentIndex = program.stopped[1] + 1;
        break;

      case CLEAR:
        program.clear(interpreter);
        break;

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
        fn.setLine(10, new CodeLine(Collections.singletonList(new Statement(program, Kind.RETURN, assignment.children[1]))));
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
      case DUMP:
        if (program.lastException != null) {
          program.lastException.printStackTrace();
          program.lastException = null;
        } else {
            program.println("\n" + program.getSymbolMap());

        /*  for (int i = 0; i < program.arrays.length; i++) {
            if (!program.arrays[i].isEmpty()) {
              program.println((i + 1) + ": " + program.arrays[i]);
            }
          } */
        }
        break;

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

      case IF:
        if (evalDouble(interpreter, 0) == 0.0) {
          interpreter.currentLine++;
          interpreter.currentIndex = 0;
        } else if (children.length == 2) {
          interpreter.currentLine = (int) evalDouble(interpreter, 1);
          interpreter.currentIndex = 0;
        }
        break;

      case LIST: {
        program.print(program.toString());
        break;
      }

      case LOAD:
        load(interpreter);
        break;

      case NEXT:
        loopEnd(interpreter);
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
            interpreter.dataStatement = program.main.find(Kind.DATA, null, interpreter.dataPosition);
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

      case RUN:
        program.clear(interpreter);

        interpreter.currentLine = children.length == 0 ? 0 : (int) evalDouble(interpreter,0);
        interpreter.currentIndex = 0;
        break;

      case SAVE:
        program.save(children.length == 0 ? null : evalString(interpreter,0));
        break;

      case STOP:
        program.stopped = new int[]{interpreter.currentLine, interpreter.currentIndex};
        program.println("\nSTOPPED in " + interpreter.currentLine + ":" + interpreter.currentIndex);
        interpreter.currentLine = Integer.MAX_VALUE;
        interpreter.currentIndex = 0;
        break;
      case TRON:
        program.trace = true;
        break;
      case TROFF:
        program.trace = false;
        break;

      default:
        throw new RuntimeException("Unimplemented statement: " + kind);
    }
    if (program.trace) {
      program.println();
    }
    return null;
  }



  void load(Interpreter interpreter) {
    String line = null;
    try {
      URLConnection connection = new URL(evalString(interpreter, 0)).openConnection();
      connection.setDoInput(true);
      InputStream is = connection.getInputStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
      while (null != (line = reader.readLine())) {
        try {
          AsdeShell.processInputLine(interpreter, line);
        } catch (Exception e) {
          program.println(line);
          program.println(e.getMessage());
        }
      }
      reader.close();
      is.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  void loopEnd(Interpreter interpreter) {
    for (int i = interpreter.nextSubIndex; i < Math.max(children.length, 1); i++) {
      String name = children.length == 0 ? null : children[i].toString();
      StackEntry entry;
      while (true) {
        if (interpreter.stack.isEmpty()
            || interpreter.stack.get(interpreter.stack.size() - 1).forVariable == null) {
          throw new RuntimeException("NEXT " + name + " without FOR.");
        }
        entry = interpreter.stack.remove(interpreter.stack.size() - 1);
        if (name == null || entry.forVariableName.equals(name)) {
          break;
        }
      }
      double current = ((Double) entry.forVariable.get(interpreter)) + entry.step;
      entry.forVariable.set(interpreter, current);
      if (Math.signum(entry.step) != Math.signum(Double.compare(current, entry.end))) {
        interpreter.stack.add(entry);
        interpreter.currentLine = entry.lineNumber;
        interpreter.currentIndex = entry.statementIndex + 1;
        break;
      }
    }
    interpreter.nextSubIndex = 0;
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
