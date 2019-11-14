package org.kobjects.asde.lang;

import org.kobjects.annotatedtext.AnnotatedString;
import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.event.StartStopListener;
import org.kobjects.asde.lang.statement.ElseStatement;
import org.kobjects.asde.lang.statement.EndIfStatement;
import org.kobjects.asde.lang.statement.EndStatement;
import org.kobjects.asde.lang.statement.ForStatement;
import org.kobjects.asde.lang.statement.IfStatement;
import org.kobjects.asde.lang.statement.NextStatement;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.statement.OnStatement;
import org.kobjects.asde.lang.type.CodeLine;
import org.kobjects.asde.lang.type.Function;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.PropertyDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * In the main package because of the direct interaction with programControl.
 */
public class FunctionImplementation implements Function, Declaration {

  public static final int MAX_LINE_NUMBER = 99999;

  public final Program program;
  FunctionType type;
  public String[] parameterNames;
  private TreeMap<Integer, CodeLine> code = new TreeMap<>();
  int localVariableCount;
  private StaticSymbol declaringSymbol;

  public FunctionImplementation(Program program, FunctionType type, String... parameterNames) {
    this.program = program;
    this.type = type;
    this.parameterNames = parameterNames;
  }

  public void validate(FunctionValidationContext functionValidationContext) {
    int indent = 0;
    for (Map.Entry<Integer, CodeLine> entry : code.entrySet()) {
      int addLater = 0;
      CodeLine line = entry.getValue();
      for (int i = 0; i < line.length(); i++) {
        Node statement = line.get(i);
        boolean isLast = i == line.length() - 1;

        if (statement instanceof ElseStatement && ((ElseStatement) statement).multiline && indent > 0) {
          addLater++;
          indent--;
        } else if (statement instanceof ForStatement
            || (statement instanceof OnStatement && isLast)
            || (statement instanceof IfStatement
            && ((IfStatement) statement).multiline
            && !((IfStatement) statement).elseIf)) {
          addLater++;
        } else if (statement instanceof NextStatement || statement instanceof EndIfStatement || statement instanceof EndStatement) {
          if (addLater > 0) {
            addLater--;
          } else if (indent > 0) {
            indent--;
          }
        }
        statement.resolve(functionValidationContext, null, entry.getKey(), i);
      }
      line.setIndent(indent);
      indent += addLater;
    }
    localVariableCount = functionValidationContext.getLocalVariableCount();
  }

  @Override
  public FunctionType getType() {
    return type;
  }


  /**
   * Calls this method with a new evaluationContext.
   */
  @Override
  public Object call(EvaluationContext callerContext, int parameterCount) {
    return callImpl(new EvaluationContext(callerContext, this, null));
  }

  public int getLocalVariableCount() {
    return localVariableCount;
  }

  public Object callImpl(EvaluationContext newContext) {
    try {
      ProgramControl control = newContext.control;
      //      if (newContext.currentLine > -1) {
      CodeLine codeLine;
      while (null != (codeLine = findNextLine(newContext.currentLine)) && !Thread.currentThread().isInterrupted()) {
        newContext.currentLine = codeLine.getNumber();
        if (control.getState() != ProgramControl.State.RUNNING) {
          if (control.state == ProgramControl.State.STEP) {
            control.state = ProgramControl.State.PAUSED;
            for (StartStopListener startStopListener : control.startStopListeners) {
              startStopListener.programPaused();
            }
          }
          control.program.console.highlight(this, newContext.currentLine);
          while (control.state == ProgramControl.State.PAUSED) {
            try {
              Thread.sleep(100);
            } catch (InterruptedException e) {
              break;
            }
          }
          if (control.state == ProgramControl.State.ABORTING) {
            throw new ForcedStopException(null);
          }
        }
        ProgramControl.runCodeLineImpl(codeLine, newContext);
      }
      //    }
      return newContext.returnValue;
    } catch (ForcedStopException e) {
      throw e;
    } catch (Exception e) {
      throw new WrappedExecutionException(this, newContext.currentLine, e);
    }
  }


  public void toString(AnnotatedStringBuilder sb, String name, Map<Node, Exception> errors) {
    boolean sub = type.getReturnType() == Types.VOID;
    String kind = sub ? "SUB" : "FUNCTION";
    if (name != null) {
      sb.append(kind);
      sb.append(' ');
      sb.append(name);
      sb.append("(");
      for (int i = 0; i < parameterNames.length; i++) {
        if (i != 0) {
          sb.append(", ");
        }
        sb.append(type.getParameterType(i).toString());
        sb.append(' ');
        sb.append(parameterNames[i]);
      }
      sb.append(")");
      if (!sub) {
        sb.append(" -> ");
        sb.append(type.getReturnType().toString());
      }
      sb.append('\n');
    }

    for (Map.Entry<Integer, CodeLine> entry : code.entrySet()) {
      sb.append(String.valueOf(entry.getKey()));
      sb.append(' ');
      entry.getValue().toString(sb, errors, true, true);
      sb.append('\n');
    }

    if (name != null) {
      sb.append("END ").append(kind).append("\n\n");
    }
  }

  public String toString() {
    AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
    toString(asb, null, Collections.emptyMap());
    return asb.toString();
  }

  public synchronized void deleteLine(int lineNumber) {
    code.remove(lineNumber);
    if (declaringSymbol != null) {
      program.notifySymbolChanged(declaringSymbol);
    }
  }

  public synchronized void setLine(CodeLine line) {
    code.put(line.getNumber(), line);
  }

  public Map.Entry<Integer, CodeLine> ceilingEntry(int i) {
    return code.ceilingEntry(i);
  }

  public synchronized CodeLine getExactLine(int lineNumber) {
    return code.get(lineNumber);
  }

  public synchronized CodeLine findNextLine(int i) {
    Map.Entry<Integer, CodeLine> entry = code.ceilingEntry(i);
    return entry == null ? null : entry.getValue();
  }

  public synchronized CodeLine findLineBefore(int i) {
    Map.Entry<Integer, CodeLine> entry = code.floorEntry(i - 1);
    return entry == null ? null : entry.getValue();
  }

  public void clear() {
    code = new TreeMap<>();
  }

  public int getLineCount() {
    return code.size();
  }


  public void renumber(int first, int last, int newStart, int step) {
    int currentNumber = newStart;

    for (CodeLine line : code.subMap(first, last + 1).values()) {
      line.setNumber(currentNumber);
      currentNumber += step;
    }

    TreeMap<Integer, Integer> renumberMap = new TreeMap<>();
    TreeMap<Integer, CodeLine> renumbered = new TreeMap<>();
    for (Map.Entry<Integer, CodeLine> entry: code.entrySet()) {
      CodeLine line = entry.getValue();
      renumbered.put(line.getNumber(), line);
      renumberMap.put(entry.getKey(), line.getNumber());
    }

    code = renumbered;

    for (Node statement : allStatements()) {
      statement.renumber(renumberMap);
    }




    if (declaringSymbol != null) {
      program.notifySymbolChanged(declaringSymbol);
    }
  }


  public Node find(StatementMatcher matcher, int... position) {
    StatementSearch search = new StatementSearch(this) {
      @Override
      public boolean statementMatches(CodeLine line, int index, Node statement) {
        return matcher.statementMatches(line, index, statement);
      }
    };
    Node result = search.find(position[0], position[1]);
    position[0] = search.lineNumber;
    position[1] = search.index;
    return result;
  }

  public void setType(FunctionType functionType) {
    this.type = functionType;
  }


  public Iterable<Node> allStatements() {
    return statements(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

  public Iterable<Node> statements(int fromLine, int fromIndex, int toLine, int toIndex) {
    return () -> new StatementIterator(fromLine, fromIndex, toLine, toIndex);
  }

  public Iterable<Node> descendingStatements(int fromLine, int fromIndex, int toLine, int toIndex) {
    return () -> new DescendingStatementIterator(fromLine, fromIndex, toLine, toIndex);
  }

  public void setDeclaringSymbol(StaticSymbol symbol) {
    this.declaringSymbol = symbol;
  }

  public boolean isMethod() {
    return declaringSymbol instanceof PropertyDescriptor;
  }

  public StaticSymbol getDeclaringSymbol() {
    return declaringSymbol;
  }

  public int countLines(int firstLine, int lastLine) {
    return code.subMap(firstLine, lastLine == Integer.MAX_VALUE ? lastLine : (lastLine + 1)).size();
  }

  public synchronized Iterable<CodeLine> allLines() {
    ArrayList<CodeLine> result = new ArrayList<>();
    result.addAll(code.values());
    return result;
  }

  class StatementIterator implements Iterator<Node> {
    final Iterator<Map.Entry<Integer, CodeLine>> lineIterator;

    private Node next;
    private Map.Entry<Integer, CodeLine> currentLine;
    int index;
    int toLine;
    int toIndex;

    StatementIterator(int fromLine, int fromIndex, int toLine, int toIndex) {
      lineIterator = code.subMap(fromLine, true, toLine, true).entrySet().iterator();
      currentLine = lineIterator.hasNext() ? lineIterator.next() : null;
      index = fromIndex;
      this.toLine = toLine;
      this.toIndex = toIndex;
    }

    public boolean hasNext() {
      if (next != null) {
        return true;
      }
      if (currentLine == null) {
        return false;
      }
      while (index >= currentLine.getValue().length()) {
        if (!lineIterator.hasNext()) {
          return false;
        }
        currentLine = lineIterator.next();
        index = 0;
      }
      if (currentLine.getKey() == toLine && index >= toIndex) {
        return false;
      }
      next = currentLine.getValue().get(index++);
      return true;
    }

    public Node next() {
      if (next == null && !hasNext()) {
        throw new IndexOutOfBoundsException();
      }
      Node result = next;
      next = null;
      return result;
    }
  }

  class DescendingStatementIterator implements Iterator<Node> {
    final Iterator<Map.Entry<Integer, CodeLine>> lineIterator;

    private Node next;
    private Map.Entry<Integer, CodeLine> currentLine;
    int index;
    int toLine;
    int toIndex;

    DescendingStatementIterator(int fromLine, int fromIndex, int toLine, int toIndex) {
      lineIterator = code.subMap(toLine, true, fromLine, true).descendingMap().entrySet().iterator();
      currentLine = lineIterator.hasNext() ? lineIterator.next() : null;
      index = fromIndex;
      this.toLine = toLine;
      this.toIndex = toIndex;
    }

    public boolean hasNext() {
      if (next != null) {
        return true;
      }
      if (currentLine == null) {
        return false;
      }
      while (index < 0) {
        if (!lineIterator.hasNext()) {
          return false;
        }
        currentLine = lineIterator.next();
        index = currentLine.getValue().length() - 1;
      }
      if (currentLine.getKey() == toLine && index <= toIndex) {
        return false;
      }
      next = currentLine.getValue().get(index--);
      return true;
    }

    public Node next() {
      if (next == null && !hasNext()) {
        throw new IndexOutOfBoundsException();
      }
      Node result = next;
      next = null;
      return result;
    }
  }

  public int[] getLineNumberRange() {
    return code.size() == 0 ? null : new int[] {code.firstKey(), code.lastKey()};
  }

}
