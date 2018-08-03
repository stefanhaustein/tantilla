package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.BasicShell;
import org.kobjects.asde.lang.DefFn;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.StackEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class Statement extends Node {

  public enum Type {
    CLEAR, CONTINUE,
    DATA, DIM, DEF, DUMP,
    END,
    FOR,
    GOTO, GOSUB,
    IF, INPUT,
    LET, LIST, LOAD,
    NEW, NEXT,
    ON,
    PRINT,
    READ, REM, RESTORE, RETURN, RUN,
    STOP,
    TRON, TROFF
  }

  final Program program;
  public final Type type;
  final String[] delimiter;

  public Statement(Program program, Type type, String[] delimiter, Node... children) {
    super(children);
    this.program = program;
    this.type = type;
    this.delimiter = delimiter;
  }

  public Statement(Program program, Type type, Node... children) {
    this(program, type, null, children);
  }

  public Object eval(Interpreter interpreter) {
    if (type == null) {
      return null;
    }

    if (program.trace) {
      program.print(interpreter.currentLine + ":" + interpreter.currentIndex + ": " + this);
    }

    switch (type) {
      case CONTINUE:
        if (program.stopped == null) {
          throw new RuntimeException("Not stopped.");
        }
        interpreter.currentLine = program.stopped[0];
        interpreter.currentIndex = program.stopped[1] + 1;
        break;

      case CLEAR:
        program.clear();
        break;

      case DEF: {
        DefFn f = new DefFn(program, children[0]);
        program.functionDefinitions.put(f.name, f);
        break;
      }
      case DATA:
      case DIM:   // We just do dynamic expansion as needed.
      case REM:
        break;

      case DUMP:
        if (program.lastException != null) {
          program.lastException.printStackTrace();
          program.lastException = null;
        } else {
          synchronized (program.variables) {
            program.println("\n" + program.variables);
          }
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

      case FOR:
        loopStart(interpreter);
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

      case LET: {
        ((AssignableNode) children[0]).set(interpreter, children[1].eval(interpreter));
        if (program.trace) {
          program.print (" // " + children[0].eval(interpreter));
        }
        break;
      }
      case LIST:
        list();
        break;

      case LOAD:
        load(interpreter);
        break;

      case NEW:
        program.clear();
        program.main.code.clear();
        break;

      case NEXT:
        loopEnd(interpreter);
        break;

      case INPUT:
        input(interpreter);
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
            interpreter.dataStatement = find(Type.DATA, null, interpreter.dataPosition);
            if (interpreter.dataStatement == null) {
              throw new RuntimeException("Out of data.");
            }
          }
          ((Variable) children[i]).set(interpreter, interpreter.dataStatement.children[interpreter.dataPosition[2]++].eval(interpreter));
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
        break;

      case RUN:
        program.clear();
        interpreter.currentLine = children.length == 0 ? 0 : (int) evalDouble(interpreter,0);
        interpreter.currentIndex = 0;
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
        throw new RuntimeException("Unimplemented statement: " + type);
    }
    if (program.trace) {
      program.println();
    }
    return null;
  }

  Statement find(Type type, String name, int[] position) {
    Map.Entry<Integer, List<Statement>> entry;
    while (null != (entry = program.main.code.ceilingEntry(position[0]))) {
      position[0] = entry.getKey();
      List<Statement> list = entry.getValue();
      while (position[1] < list.size()) {
        Statement statement = list.get(position[1]);
        if (statement.type == type) {
          if (name == null || statement.children.length == 0) {
            return statement;
          }
          for (int i = 0; i < statement.children.length; i++) {
            if (statement.children[i].toString().equalsIgnoreCase(name)) {
              position[2] = i;
              return statement;
            }
          }
        }
        position[1]++;
      }
      position[0]++;
      position[1] = 0;
    }
    return null;
  }

  void list() {
    program.println();
    for (Map.Entry<Integer, List<Statement>> entry : program.main.code.entrySet()) {
      program.print(entry.getKey());
      List<Statement> line = entry.getValue();
      for (int i = 0; i < line.size(); i++) {
        program.print(i == 0 || line.get(i - 1).type == Type.IF ? " " : " : ");
        program.print(line.get(i));
      }
      program.println();
    }
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
          BasicShell.processInputLine(interpreter, line);
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

  void loopStart(Interpreter interpreter) {
    double current = evalDouble(interpreter,1);
    ((Variable) children[0]).set(interpreter, current);
    double end = evalDouble(interpreter, 2);
    double step = children.length > 3 ? evalDouble(interpreter, 3) : 1.0;
    if (Math.signum(step) == Math.signum(Double.compare(current, end))) {
      int nextPosition[] = new int[3];
      if (find(Type.NEXT, children[0].toString(), nextPosition) == null) {
        throw new RuntimeException("FOR without NEXT");
      }
      interpreter.currentLine = nextPosition[0];
      interpreter.currentIndex = nextPosition[1];
      interpreter.nextSubIndex = nextPosition[2] + 1;
    } else {
      StackEntry entry = new StackEntry();
      entry.forVariable = (Variable) children[0];
      entry.end = end;
      entry.step = step;
      entry.lineNumber = interpreter.currentLine;
      entry.statementIndex = interpreter.currentIndex;
      interpreter.stack.add(entry);
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
        if (name == null || entry.forVariable.name.equals(name)) {
          break;
        }
      }
      double current = ((Double) entry.forVariable.eval(interpreter)) + entry.step;
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
      if (type == Type.INPUT && child instanceof Variable) {
        if (i <= 0 || i > delimiter.length || !delimiter[i-1].equals(", ")) {
          program.print("? ");
        }
        Variable variable = (Variable) child;
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
  public Class<?> returnType() {
    return Void.class;
  }

  @Override
  public String toString() {
    if (type == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    sb.append(type.name());
    if (children.length > 0) {
      sb.append(' ');
      sb.append(children[0]);
      for (int i = 1; i < children.length; i++) {
        sb.append((delimiter == null || i > delimiter.length) ? ", " : delimiter[i - 1]);
        sb.append(children[i]);
      }
      if (delimiter != null && delimiter.length == children.length) {
        sb.append(delimiter[delimiter.length - 1]);
      }
    }
    return sb.toString();
  }
}
