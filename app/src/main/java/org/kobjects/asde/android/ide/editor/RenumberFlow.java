package org.kobjects.asde.android.ide.editor;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.widget.TextValidator;
import org.kobjects.asde.lang.FunctionImplementation;
import org.kobjects.asde.lang.StaticSymbol;

public class RenumberFlow {

  private final MainActivity mainActivity;
  private final StaticSymbol symbol;
  private final int firstLine;
  private final int lastLine;
  private final FunctionImplementation functionImplementation;
  private int newStart;
  private int step;
  private final int lineCount;
  private int spaceAvailable;

  public static void start(MainActivity mainActivity, StaticSymbol symbol, int firstLine, int lastLine) {
    new RenumberFlow(mainActivity, symbol, firstLine, lastLine).start();
  }

  RenumberFlow(MainActivity mainActivity, StaticSymbol symbol, int firstLine, int lastLine) {
    this.mainActivity = mainActivity;
    this.symbol = symbol;
    this.firstLine = firstLine;
    this.lastLine = lastLine;
    functionImplementation = (FunctionImplementation) symbol.getValue();

    lineCount = functionImplementation.countLines(firstLine, lastLine);
  }

  private String validateFirstLine(String text) {
    try {
      newStart = Integer.parseInt(text);
    } catch (Exception e) {
      return "Integer required";
    }

    if (newStart < 1) {
      return "Minimum: 1";
    }

    if (newStart > FunctionImplementation.MAX_LINE_NUMBER) {
      return "Maximum: " + FunctionImplementation.MAX_LINE_NUMBER;
    }

    spaceAvailable = functionImplementation.linesAvailableAtExcluding(newStart, firstLine, lastLine);

    if (lineCount > spaceAvailable) {
      return lineCount + " lines can't be renumbered into " + spaceAvailable + " numbers available at " + newStart;
    }

    return null;
  }

  private String validateStep(String text) {
    try {
      step = Integer.parseInt(text);
    } catch (Exception e) {
      return "Integer required";
    }
    if (step < 1) {
      return "Step must be at least 1";
    }
    if (step > 10000) {
      return "Step out of range";
    }
    int maxStep = spaceAvailable / lineCount;
    if (step > maxStep) {
      return "Step must be <= " + maxStep + " for space at " + newStart;
    }
    return null;
  }

  private void start() {
    new InputFlowBuilder(mainActivity,"Renumber")
        .setMessage("Lines " + firstLine + " - " + lastLine)
        .setPositiveLabel("Renumber")
        .addInput("First Line", firstLine, new TextValidator() {
          @Override
          public String validate(String text) {
            return validateFirstLine(text);
          }
        })
        .addInput("Step", 10, new TextValidator() {
          @Override
          public String validate(String text) {
            return validateStep(text);
          }
        })
        .start(result -> {
              ((FunctionImplementation) symbol.getValue()).renumber(
                  firstLine,
                  lastLine,
                  Integer.parseInt(result[0]),
                  Integer.parseInt(result[1]));
            }
        );

  }

}
