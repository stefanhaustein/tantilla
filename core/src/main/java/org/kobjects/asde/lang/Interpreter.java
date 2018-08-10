package org.kobjects.asde.lang;

import org.kobjects.asde.lang.node.Statement;
import org.kobjects.asde.lang.symbol.GlobalSymbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Interpreter {
    public final Program program;
    public int currentLine;
    public int currentIndex;
    public int nextSubIndex;  // index within next when skipping a for loop; reset in next
    public ArrayList<StackEntry> stack = new ArrayList<>();

    public int[] dataPosition = new int[3];
    public Statement dataStatement;
    CallableUnit callableUnit;
    public Object returnValue;
    public Object[] locals;

    public Interpreter(Program program) {
        this.program = program;
    }

    Thread interpreterThread;
    List<StartStopListener> startStopListeners = new ArrayList<>();

    public void addStartStopListener(StartStopListener startStopListener) {
        startStopListeners.add(startStopListener);
    }


    public boolean isRunning() {
        return interpreterThread != null;
    }

    public void stop() {
        if (interpreterThread != null) {
            interpreterThread.interrupt();
            interpreterThread = null;
            for (StartStopListener startStopListener : startStopListeners) {
                startStopListener.programStopped();
            }
        }
    }

    public void runAsync(CallableUnit callableUnit) {
        runStatementsAsync(Collections.singletonList(new Statement(program, Statement.Kind.RUN)), callableUnit);
    }

    public GlobalSymbol.Scope getSymbolScope() {
        return currentLine == -2 ? GlobalSymbol.Scope.PERSISTENT : GlobalSymbol.Scope.TRANSIENT;
    }

    public void runStatementsAsync(final List<Statement> statements, final CallableUnit callableUnit) {
        stop();
        interpreterThread = new Thread(new Runnable() {
            @Override
            public void run() {
                currentLine = -2;
                Interpreter.this.callableUnit = callableUnit;
                runStatementsImpl(statements);
                runCallableUnit();
                if (interpreterThread != null) {
                    interpreterThread = null;
                    for (StartStopListener startStopListener : startStopListeners) {
                        startStopListener.programStopped();
                    }
                }
            }
        });

        for (StartStopListener startStopListener : startStopListeners) {
            startStopListener.programStarted();
        }
        interpreterThread.start();
    }

    private void runStatementsImpl(List<Statement> statements) {
        int line = currentLine;

        while (currentIndex < statements.size() && !Thread.currentThread().isInterrupted()) {
            int index = currentIndex;
            statements.get(index).eval(this);
            if (currentLine != line) {
                return;  // Goto or similar out of the current line
            }
            if (currentIndex == index) {
                currentIndex++;
            }
        }
        currentIndex = 0;
        currentLine++;
    }

    private void runCallableUnit() {
        if (currentLine > -1) {
            Map.Entry<Integer, CodeLine> entry;
            while (null != (entry = callableUnit.ceilingEntry(currentLine)) && !Thread.currentThread().isInterrupted()) {
                currentLine = entry.getKey();
                runStatementsImpl(entry.getValue().statements);
            }
        }

    }

    public Object call(CallableUnit callableUnit, Object[] locals) {
        Interpreter sub = new Interpreter(program);
        sub.locals = locals;
        sub.callableUnit = callableUnit;
        sub.runCallableUnit();
        return sub.returnValue;
    }

}
