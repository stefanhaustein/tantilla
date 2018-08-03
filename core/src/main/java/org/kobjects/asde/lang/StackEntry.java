package org.kobjects.asde.lang;

import org.kobjects.asde.lang.node.Variable;

public class StackEntry {
  public int lineNumber;
  public int statementIndex;
  public Variable forVariable;
  public double step;
  public double end;
}
