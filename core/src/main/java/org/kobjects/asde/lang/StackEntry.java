package org.kobjects.asde.lang;

import org.kobjects.asde.lang.symbol.ResolvedSymbol;

public class StackEntry {
  public int lineNumber;
  public int statementIndex;
  public ResolvedSymbol forVariable;
  public double step;
  public double end;
  public String forVariableName;
}
