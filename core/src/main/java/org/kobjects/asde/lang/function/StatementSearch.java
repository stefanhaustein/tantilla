package org.kobjects.asde.lang.function;

public abstract class StatementSearch implements StatementMatcher {

  /*
  final FunctionImplementation function;

  public int lineNumber;
  public int index;

  public StatementSearch(FunctionImplementation function) {
    this.function = function;
  }

  public Node find(final int startLine, final int startIndex) {
    lineNumber = startLine;
    index = startIndex;
    CodeLine line;
    while (null != (line = function.findNextLine(lineNumber))) {
      lineNumber = line.getNumber();
      while (index < line.length()) {
        Node statement = line.get(index);
        if (statementMatches(line, index, statement)) {
          return statement;
        }
        index++;
      }
      lineNumber++;
      index = 0;
    }
    return null;
  }*/
}
