package org.kobjects.asde.lang;

import org.kobjects.asde.lang.node.Identifier;

public class StackEntry {
  public int lineNumber;
  public int statementIndex;
  public Identifier forVariable;
  public double step;
  public double end;
}
