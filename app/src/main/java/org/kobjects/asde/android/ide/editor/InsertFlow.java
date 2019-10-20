package org.kobjects.asde.android.ide.editor;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.lang.FunctionImplementation;
import org.kobjects.asde.lang.StaticSymbol;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.parser.StatementParser;
import org.kobjects.asde.lang.statement.UnparseableStatement;
import org.kobjects.asde.lang.type.CodeLine;
import org.kobjects.expressionparser.ExpressionParser;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class InsertFlow {
  public static void start(MainActivity mainActivity, StaticSymbol symbol, int targetLine) {

    FunctionImplementation functionImplementation = (FunctionImplementation) symbol.getValue();
    int lastAvailableLine = functionImplementation.findNextLine(targetLine + 1) == null ? FunctionImplementation.MAX_LINE_NUMBER : functionImplementation.findNextLine(targetLine + 1).getNumber() - 1;

    int lineCount = mainActivity.copyBuffer.size();
    if (lineCount > lastAvailableLine - targetLine + 1) {
      new InputFlowBuilder(mainActivity, "Insufficient Space")
          .setMessage("Can't fit " + mainActivity.copyBuffer.size() + " lines between line numbers "
              + targetLine + " and " + lastAvailableLine + ".")
          .start(null);
    }

    StatementParser statementParser = mainActivity.program.parser;

    int step = 10;
    while (step > 1) {
      int tentativeFirstLine = ((targetLine + step - 1) / step) * step;
      if (tentativeFirstLine + (lineCount - 1) * step <= lastAvailableLine) {
        targetLine = tentativeFirstLine;
        break;
      }
      step /= 2;
    }

    TreeMap<Integer, Integer> renumberMap = new TreeMap<>();

    int lineNumber = targetLine;
    for (Map.Entry<Integer, String> entry : mainActivity.copyBuffer.entrySet()) {
      renumberMap.put(entry.getKey(), lineNumber);
      String line = entry.getValue();
      ExpressionParser.Tokenizer tokenizer = statementParser.createTokenizer(line);
      tokenizer.nextToken();
      int pos = tokenizer.currentPosition;
      try {
        List<? extends Node> statements = statementParser.parseStatementList(tokenizer, functionImplementation);
        functionImplementation.setLine(new CodeLine(lineNumber, statements));
      } catch (Exception e) {
        functionImplementation.setLine(new CodeLine(lineNumber, Collections.singletonList(new UnparseableStatement(line.substring(pos), e))));
      }
      lineNumber += step;
    }

    for (Node statement : functionImplementation.statements(targetLine, 0, lastAvailableLine, Integer.MAX_VALUE)) {
      statement.renumber(renumberMap);
    }


    mainActivity.program.notifySymbolChanged(symbol);
  }
}
