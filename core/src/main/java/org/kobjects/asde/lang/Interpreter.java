package org.kobjects.asde.lang;

import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.node.Statement;
import org.kobjects.asde.lang.symbol.GlobalSymbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Interpreter {
    public final CallableUnit callableUnit;
    public final Program program;
    public final Object[] locals;
    public int currentLine;
    public int currentIndex;
    public int nextSubIndex;  // index within next when skipping a for loop; reset in next
    public ArrayList<StackEntry> stack = new ArrayList<>();

    public int[] dataPosition = new int[3];
    public Statement dataStatement;
    public Object returnValue;

    public Interpreter(Program program, CallableUnit callableUnit, Object[] locals) {
        this.program = program;
        this.callableUnit = callableUnit;
        this.locals = locals;
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

    public void runAsync() {
        program.clear(this);
        runAsync(0);
    }

    public void runAsync(final Runnable runnable) {
        stop();
        interpreterThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception e) {
                    e.printStackTrace();
                    program.console.print(e.toString());
                }
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


    public void runAsync(final int runLine) {
            runAsync(new Runnable() {
                @Override
                public void run() {
                    currentLine = runLine;
                    runCallableUnit();
                }
            });
    }

    public GlobalSymbol.Scope getSymbolScope() {
        return currentLine == -2 ? GlobalSymbol.Scope.PERSISTENT : GlobalSymbol.Scope.TRANSIENT;
    }

    public void runStatementsAsync(final List<? extends Node> statements, final Interpreter programInterpreter) {
        runAsync(new Runnable() {
            @Override
            public void run() {
                currentLine = -2;
                runStatementsImpl(statements);
                if (currentLine >= 0) {
                    programInterpreter.runAsync(currentLine);
                }
            }
        });
    }

    private void runStatementsImpl(List<? extends Node> statements) {
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
        Interpreter sub = new Interpreter(program, callableUnit, locals);
        sub.runCallableUnit();
        return sub.returnValue;
    }

}
