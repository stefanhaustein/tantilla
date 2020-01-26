package org.kobjects.asde.lang.function;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.statement.BlockStatement;
import org.kobjects.asde.lang.statement.Statement;
import org.kobjects.asde.lang.symbol.Declaration;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.runtime.ForcedStopException;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.program.ProgramControl;
import org.kobjects.asde.lang.symbol.StaticSymbol;
import org.kobjects.asde.lang.runtime.WrappedExecutionException;
import org.kobjects.asde.lang.runtime.StartStopListener;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.PropertyDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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
  private List<Statement> code = new ArrayList<>();
  public int localVariableCount;
  private StaticSymbol declaringSymbol;

  public FunctionImplementation(Program program, FunctionType type, String... parameterNames) {
    this.program = program;
    this.type = type;
    this.parameterNames = parameterNames;
  }

  public void validate(FunctionValidationContext functionValidationContext) {
    for (int i = 0; i < code.size(); i++) {
      Statement statement = code.get(i);
      statement.resolve(functionValidationContext, null, i);
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
      while (newContext.currentLine < code.size() && !Thread.currentThread().isInterrupted()) {
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
        ProgramControl.runCodeLineImpl(code.get(newContext.currentLine), newContext);
      }
      //    }
      return newContext.returnValue;
    } catch (ForcedStopException e) {
      throw e;
    } catch (Exception e) {
      throw new WrappedExecutionException(this, newContext.currentLine, e);
    }
  }


  public synchronized void toString(AnnotatedStringBuilder sb, String name, Map<Node, Exception> errors) {
    boolean sub = type.getReturnType() == Types.VOID;
    if (name != null) {
      sb.append("def ");
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
      sb.append(":\n");
    }

    for (Statement statement : code) {
      for (int i = 0; i < statement.getIndent(); i++) {
        sb.append(' ');
      }
      statement.toString(sb, errors, true);
      sb.append('\n');
    }

    if (name != null) {
      sb.append("end\n\n");
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

/*
  public synchronized void setLine(CodeLine line) {
    code.put(line.getNumber(), line);
  }

  public Map.Entry<Integer, CodeLine> ceilingEntry(int i) {
    return code.ceilingEntry(i);
  }*/

  public synchronized Statement getLine(int lineNumber) {
    return code.get(lineNumber);
  }
/*
  public synchronized CodeLine findNextLine(int i) {
    Map.Entry<Integer, CodeLine> entry = code.ceilingEntry(i);
    return entry == null ? null : entry.getValue();
  }

  public synchronized CodeLine findLineBefore(int i) {
    Map.Entry<Integer, CodeLine> entry = code.floorEntry(i - 1);
    return entry == null ? null : entry.getValue();
  }
*/
  public void clear() {
    code = new ArrayList<>();
  }

  public int getLineCount() {
    return code.size();
  }

/*
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
*/
  public void setType(FunctionType functionType) {
    this.type = functionType;
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

  /*
  public int countLines(int firstLine, int lastLine) {
    return code.subMap(firstLine, lastLine == Integer.MAX_VALUE ? lastLine : (lastLine + 1)).size();
  }*/

  public synchronized Iterable<Statement> allLines() {
    ArrayList<Statement> result = new ArrayList<>();
    result.addAll(code);
    return result;
  }

  public synchronized void appendStatement(Statement statement) {
    code.add(statement);
    if (declaringSymbol != null) {
      program.notifySymbolChanged(declaringSymbol);
    }
  }

  public synchronized void setLine(int lineNumber, Statement statement) {
    if (lineNumber < code.size()) {
      code.set(lineNumber, statement);
    } else {
      code.add(statement);
    }
    if (declaringSymbol != null) {
      program.notifySymbolChanged(declaringSymbol);
    }
  }

  public synchronized void insertLine(int lineNumber, Statement statement) {
    if (lineNumber < code.size()) {
      code.add(lineNumber, statement);
    } else {
      code.add(statement);
    }
    if (declaringSymbol != null) {
      program.notifySymbolChanged(declaringSymbol);
    }
  }

}
