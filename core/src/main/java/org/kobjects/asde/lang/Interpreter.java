package org.kobjects.asde.lang;

import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.statement.LegacyStatement;
import org.kobjects.asde.lang.symbol.GlobalSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Interpreter {
    public final CallableUnit callableUnit;
    public int currentLine;
    public int currentIndex;
    public int nextSubIndex;  // index within next when skipping a for loop; reset in next
    public ArrayList<StackEntry> stack = new ArrayList<>();

    public int[] dataPosition = new int[3];
    public LegacyStatement dataStatement;
    public Object returnValue;
    public final LocalStack localStack;
    public final ProgramControl control;

    public Interpreter(ProgramControl control, CallableUnit callableUnit, LocalStack localStack) {
        this.control = control;
        this.callableUnit = callableUnit;
        this.localStack = localStack;
    }


    public GlobalSymbol.Scope getSymbolScope() {
        return currentLine == -2 ? GlobalSymbol.Scope.PERSISTENT : GlobalSymbol.Scope.TRANSIENT;
    }

    Object runStatementsImpl(List<? extends Node> statements) {
        int line = currentLine;
        Object result = null;
        while (currentIndex < statements.size() && control.state != ProgramControl.State.ABORTING) {
            int index = currentIndex;
            result = statements.get(index).eval(this);
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
            while (null != (entry = callableUnit.ceilingEntry(currentLine)) && !Thread.currentThread().isInterrupted()) {
                currentLine = entry.getKey();
                if (control.state != ProgramControl.State.PAUSED) {
                    runStatementsImpl(entry.getValue().statements);
                } else {
                    control.program.console.trace(callableUnit, currentLine);

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
                        runStatementsImpl(entry.getValue().statements);
                    }
                }
            }
        }

    }

}
