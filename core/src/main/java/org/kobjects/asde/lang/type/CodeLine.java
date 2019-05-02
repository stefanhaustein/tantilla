package org.kobjects.asde.lang.type;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.statement.ElseStatement;
import org.kobjects.asde.lang.statement.EndStatement;
import org.kobjects.asde.lang.statement.IfStatement;
import org.kobjects.asde.lang.statement.OnStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CodeLine implements Iterable<Node> {
  private final int lineNumber;
  private Node[] statements;
  private int indent;

  public CodeLine(int lineNumber, Node... statements) {
    this.lineNumber = lineNumber;
    this.statements = statements;
  }

  public CodeLine(int lineNumber, List<? extends Node> statements) {
    this(lineNumber, statements.toArray(new Node[0]));
  }

  public void toString(AnnotatedStringBuilder sb, Map<Node, Exception> errors) {
    for (int i = 0; i < indent; i++) {
      sb.append(' ');
    }
    for (int i = 0; i < statements.length; i++) {
      if (i > 0) {
        sb.append(statements[i - 1] instanceof IfStatement
            || statements[i - 1] instanceof ElseStatement || statements[i] instanceof EndStatement ? " " : " : ");
      }
      statements[i].toString(sb, errors);
    }
    if (statements.length > 0 && statements[statements.length - 1] instanceof OnStatement) {
      sb.append(" :");
    }
  }

  @Override
  public String toString() {
    AnnotatedStringBuilder sb = new AnnotatedStringBuilder();
    toString(sb, Collections.<Node, Exception>emptyMap());
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

  public void append(Node node) {
    Node[] newStatements = new Node[statements.length + 1];
    System.arraycopy(statements, 0, newStatements, 0, statements.length);
    newStatements[statements.length] = node;
    statements = newStatements;
  }
}
