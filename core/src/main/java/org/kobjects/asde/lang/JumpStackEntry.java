package org.kobjects.asde.lang;

public class JumpStackEntry {
  public int lineNumber;
  public int statementIndex;
  public ResolvedSymbol forVariable;
  public double step;
  public double end;
  public String forVariableName;
}
