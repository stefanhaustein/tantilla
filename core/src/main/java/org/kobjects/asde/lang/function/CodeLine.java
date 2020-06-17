package org.kobjects.asde.lang.function;

import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.statement.BlockStatement;
import org.kobjects.asde.lang.statement.Statement;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CodeLine implements Iterable<Statement> {
  private int lineNumber;
  private Statement[] statements;
  public int indent;

  public CodeLine(int lineNumber, Statement... statements) {
    this.lineNumber = lineNumber;
    this.statements = statements;
  }

  public CodeLine(int lineNumber, List<? extends Node> statements) {
    this(lineNumber, statements.toArray(new Statement[0]));
  }

  public void toString(AnnotatedStringBuilder sb, Map<Node, Exception> errors, boolean indent, boolean preferAscii) {
    if (indent) {
      for (int i = 0; i < this.indent; i++) {
        sb.append(' ');
      }
    }
    for (int i = 0; i < statements.length; i++) {
      if (i > 0) {
        sb.append((statements[i - 1] instanceof BlockStatement)
            ? " " : "; ");
      }
      statements[i].toString(sb, errors, preferAscii);
    }
  }

  @Override
  public String toString() {
    AnnotatedStringBuilder sb = new AnnotatedStringBuilder();
    toString(sb, Collections.<Node, Exception>emptyMap(), true, true);
    return sb.toString();
  }

  public int length() {
    return statements.length;
  }

  public Statement get(int index) {
    return statements[index];
  }


  @Override
  public Iterator<Statement> iterator() {
    return Arrays.asList(statements).iterator();
  }

  public void setIndent(int indent) {
    this.indent = indent;
  }

  public Integer getNumber() {
    return lineNumber;
  }

  public void setNumber(int newNumber) {
    lineNumber = newNumber;
  }

  public void append(Statement node) {
    Statement[] newStatements = new Statement[statements.length + 1];
    System.arraycopy(statements, 0, newStatements, 0, statements.length);
    newStatements[statements.length] = node;
    statements = newStatements;
  }

  public void set(int i, Statement statement) {
    statements[i] = statement;
  }
}
