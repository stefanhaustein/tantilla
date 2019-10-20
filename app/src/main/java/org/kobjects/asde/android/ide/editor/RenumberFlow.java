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
  private int firstAvailableLine;
  private int lastAvailableLine;

  public static void start(MainActivity mainActivity, StaticSymbol symbol, int firstLine, int lastLine) {
    new RenumberFlow(mainActivity, symbol, firstLine, lastLine).start();
  }

  RenumberFlow(MainActivity mainActivity, StaticSymbol symbol, int firstLine, int lastLine) {
    this.mainActivity = mainActivity;
    this.symbol = symbol;
    this.firstLine = firstLine;
    this.lastLine = lastLine;
    functionImplementation = (FunctionImplementation) symbol.getValue();

    firstAvailableLine = functionImplementation.findLineBefore(firstLine) == null ? 1 : functionImplementation.findLineBefore(firstLine).getNumber() + 1;
    lastAvailableLine = functionImplementation.findNextLine(lastLine + 1) == null ? FunctionImplementation.MAX_LINE_NUMBER : functionImplementation.findNextLine(lastLine + 1).getNumber() - 1;

    lineCount = functionImplementation.countLines(firstLine, lastLine);

    step = (lastAvailableLine - newStart + 1) / lineCount;
    for (int i = 10; i > 1; i /= 2) {
      if (step > i) {
        step = i;
        break;
      }
    }
  }

  private String validateFirstLine(String text) {
    try {
      newStart = Integer.parseInt(text);
    } catch (Exception e) {
      return "Integer required";
    }

    if (newStart < firstAvailableLine) {
      return "Minimum: " + firstAvailableLine;
    }

    if (newStart > lastAvailableLine - lineCount + 1) {
      return "Maximum: " + (lastAvailableLine - lineCount + 1);
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
    int maxStep = (lastAvailableLine - newStart + 1) / lineCount;
    if (step > maxStep) {
      return "Step must be < " + (maxStep + 1) + " to fit below " + (lastAvailableLine + 1);
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
        .addInput("Step", step, new TextValidator() {
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
