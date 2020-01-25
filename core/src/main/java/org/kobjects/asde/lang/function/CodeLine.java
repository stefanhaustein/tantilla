package org.kobjects.asde.lang.function;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.statement.BlockStatement;
import org.kobjects.asde.lang.statement.OnStatement;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CodeLine implements Iterable<Node> {
  private int lineNumber;
  private Node[] statements;
  public int indent;

  public CodeLine(int lineNumber, Node... statements) {
    this.lineNumber = lineNumber;
    this.statements = statements;
  }

  public CodeLine(int lineNumber, List<? extends Node> statements) {
    this(lineNumber, statements.toArray(new Node[0]));
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

  public Node get(int index) {
    return statements[index];
  }


  @Override
  public Iterator<Node> iterator() {
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

  public void append(Node node) {
    Node[] newStatements = new Node[statements.length + 1];
    System.arraycopy(statements, 0, newStatements, 0, statements.length);
    newStatements[statements.length] = node;
    statements = newStatements;
  }

  public void set(int i, Node statement) {
    statements[i] = statement;
  }
}
