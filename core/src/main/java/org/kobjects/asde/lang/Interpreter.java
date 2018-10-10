package org.kobjects.asde.lang;

import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.statement.IoStatement;
import org.kobjects.asde.lang.statement.LegacyStatement;
import org.kobjects.asde.lang.symbol.GlobalSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Interpreter {
    public final CallableUnit callableUnit;
    public final Program program;
    public int currentLine;
    public int currentIndex;
    public int nextSubIndex;  // index within next when skipping a for loop; reset in next
    public ArrayList<StackEntry> stack = new ArrayList<>();

    public int[] dataPosition = new int[3];
    public LegacyStatement dataStatement;
    public Object returnValue;
    public final LocalStack localStack;

    public Interpreter(Program program, CallableUnit callableUnit, LocalStack localStack) {
        this.program = program;
        this.callableUnit = callableUnit;
        this.localStack = localStack;
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

    // Called from the shell
    public void runStatementsAsync(final List<? extends Node> statements, final Interpreter programInterpreter) {
        runAsync(new Runnable() {
            @Override
            public void run() {
                currentLine = -2;
                Object result = runStatementsImpl(statements);
                if (currentLine >= 0) {
                    programInterpreter.runAsync(currentLine);
                } else if (statements.size() == 0 || (!(statements.get(statements.size() - 1) instanceof IoStatement)
                        || ((IoStatement) statements.get(statements.size() - 1)).kind != IoStatement.Kind.PRINT)) {
                    programInterpreter.program.console.print(result == null ? "OK\n" : (String.valueOf(result) + "\n"));
                }

            }
        });
    }

    private Object runStatementsImpl(List<? extends Node> statements) {
        int line = currentLine;
        Object result = null;
        while (currentIndex < statements.size() && !Thread.currentThread().isInterrupted()) {
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
                runStatementsImpl(entry.getValue().statements);
            }
        }

    }

}
