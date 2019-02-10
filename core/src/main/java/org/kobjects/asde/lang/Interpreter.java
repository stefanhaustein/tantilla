package org.kobjects.asde.lang;

import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.statement.LegacyStatement;
import org.kobjects.asde.lang.type.FunctionImplementation;
import org.kobjects.asde.lang.type.CodeLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Interpreter {
    public final FunctionImplementation functionImplementation;
    public int currentLine;
    public int currentIndex;
    public int nextSubIndex;  // index within next when skipping a for loop; reset in next
    public ArrayList<JumpStackEntry> stack = new ArrayList<>();

    public int[] dataPosition = new int[3];
    public LegacyStatement dataStatement;
    public Object returnValue;
    public final LocalStack localStack;
    public final ProgramControl control;

    public Interpreter(ProgramControl control, FunctionImplementation functionImplementation, LocalStack localStack) {
        this.control = control;
        this.functionImplementation = functionImplementation;
        this.localStack = localStack;
    }


    public GlobalSymbol.Scope getSymbolScope() {
        return currentLine == -2 ? GlobalSymbol.Scope.PERSISTENT : GlobalSymbol.Scope.TRANSIENT;
    }

    Object runCodeLineImpl(CodeLine codeLine) {
        int line = currentLine;
        Object result = null;
        while (currentIndex < codeLine.length() && control.state != ProgramControl.State.ABORTING) {
            int index = currentIndex;
            result = codeLine.get(index).eval(this);
            if (currentLine != line) {
                return result;  // Goto or similar out of the current line
            }
            if (currentIndex == index) {
                currentIndex++;
            }
        }
        currentIndex = 0;
        currentLine++;
        return result;
    }

    public void runCallableUnit() {
        if (currentLine > -1) {
            Map.Entry<Integer, CodeLine> entry;
            while (null != (entry = functionImplementation.ceilingEntry(currentLine)) && !Thread.currentThread().isInterrupted()) {
                currentLine = entry.getKey();
                if (control.state != ProgramControl.State.PAUSED) {
                    runCodeLineImpl(entry.getValue());
                } else {
                    control.program.console.highlight(functionImplementation, currentLine);

                    while (control.state == ProgramControl.State.PAUSED) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }

                    if (control.state == ProgramControl.State.STEP) {
                        control.state = ProgramControl.State.PAUSED;
                    }

                    if (control.state != ProgramControl.State.ABORTING && control.state != ProgramControl.State.ENDED) {
                        runCodeLineImpl(entry.getValue());
                    }
                }
            }
        }

    }

}
