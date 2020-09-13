package org.kobjects.asde.lang.function;

import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Consumer;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.io.SyntaxColor;
import org.kobjects.asde.lang.statement.Statement;
import org.kobjects.asde.lang.classifier.DeclaredBy;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.exceptions.ForcedStopException;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.program.ProgramControl;
import org.kobjects.asde.lang.exceptions.WrappedExecutionException;
import org.kobjects.asde.lang.runtime.StartStopListener;
import org.kobjects.asde.lang.node.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * In the main package because of the direct interaction with programControl.
 */
public class UserFunction implements Callable, DeclaredBy {

  public final Program program;
  FunctionType type;
  private List<Statement> code = new ArrayList<>();
  public int localVariableCount;
  private Property declaringSymbol;

  public UserFunction(Program program, FunctionType type) {
    this.program = program;
    this.type = type;
  }

  public void validate(ValidationContext validationContext) {
    for (int i = 0; i < code.size(); i++) {
      Statement statement = code.get(i);
      statement.resolve(validationContext, i + 1);
    }
    localVariableCount = validationContext.getLocalVariableCount();
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
    return callImpl(new EvaluationContext(callerContext, this));
  }

  public int getLocalVariableCount() {
    return localVariableCount;
  }

  @Override
  public CharSequence getDocumentation() {
    // TODO
    return null;
  }

  public Object callImpl(EvaluationContext newContext) {
    System.err.println("CallImpl of " + declaringSymbol);
    for (int i = newContext.stackBase; i < newContext.dataStack.size(); i++) {
      System.err.println(" local " + i + ": " + newContext.dataStack.getObject(i));
    }

    try {
      ProgramControl control = newContext.control;
      //      if (newContext.currentLine > -1) {
      while (newContext.currentLine <= code.size() && !Thread.currentThread().isInterrupted()) {
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
        ProgramControl.runCodeLineImpl(code.get(newContext.currentLine - 1), newContext);
      }
      //    }
      return newContext.returnValue;
    } catch (ForcedStopException e) {
      throw e;
    } catch (Exception e) {
      throw new WrappedExecutionException(this, newContext.currentLine, e);
    }
  }

  public synchronized void toString(AnnotatedStringBuilder sb, String indent, boolean exportFormat, Map<Node, Exception> errors) {
    if (declaringSymbol == null) {
      for (int i = 0; i < code.size(); i++) {
        if (i > 0) {
          sb.append("; ");
        }
        code.get(i).toString(sb, errors, true);
      }
    } else {
      sb.append(indent);
      sb.append("def ", SyntaxColor.KEYWORD);
      sb.append(declaringSymbol.getName());
      sb.append(type.toString());
      sb.append(":\n");
      int lineNumber = 1;
      for (Statement statement : code) {
        if (exportFormat) {
          sb.append(indent);
        } else {
          sb.append(String.valueOf(lineNumber++));
        }
        for (int i = -1; i < statement.getIndent(); i++) {
          sb.append(' ');
        }
        statement.toString(sb, errors, true);
        sb.append('\n');
      }
      if (declaringSymbol != null) {
        sb.append("end", SyntaxColor.HIDE);
        sb.append("\n\n");
      }
    }
  }

  public String toString() {
    AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
    toString(asb, "", false, Collections.emptyMap());
    return asb.toString();
  }

  public synchronized void deleteLine(int lineNumber) {
    int index = lineNumber - 1;
    code.remove(index);
    if (declaringSymbol != null) {
      program.notifySymbolChanged(declaringSymbol);
    }
  }

  public synchronized Statement getLine(int lineNumber) {
    int index = lineNumber - 1;
    return code.get(index);
  }

  public void clear() {
    code = new ArrayList<>();
  }

  public int getLineCount() {
    return code.size();
  }

  @Override
  public void setType(FunctionType functionType) {
    this.type = functionType;
  }


  @Override
  public void setDeclaredBy(Property symbol) {
    /* The main function gets re-attached....
    if (declaringSymbol != null && declaringSymbol != symbol) {
      throw new IllegalStateException("Can't attach this function to " + symbol + "; already attached to: " + declaringSymbol);
    }*/
    this.declaringSymbol = symbol;
  }

  public Property getDeclaringSymbol() {
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
    int index = lineNumber - 1;
    if (index < code.size()) {
      code.set(index, statement);
    } else {
      code.add(statement);
    }
    if (declaringSymbol != null) {
      program.notifySymbolChanged(declaringSymbol);
    }
  }

  public synchronized void insertLine(int lineNumber, Statement statement) {
    int index = lineNumber - 1;
    if (index < code.size()) {
      code.add(index, statement);
    } else {
      code.add(statement);
    }
    if (declaringSymbol != null) {
      program.notifySymbolChanged(declaringSymbol);
    }
  }

  public synchronized void processNodes(Consumer<Node> action) {
    for (Statement statement : code) {
      statement.process(action);
    }
  }

}
