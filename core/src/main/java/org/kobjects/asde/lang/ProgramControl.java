package org.kobjects.asde.lang;

import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.statement.IoStatement;

import java.util.ArrayList;
import java.util.List;

public class ProgramControl {
    Thread interpreterThread;
    private final List<StartStopListener> startStopListeners = new ArrayList<>();
    public Program program;
    Interpreter rootInterprter;

    boolean stopped;
    boolean trace;

    public ProgramControl(Program program) {
        this.program = program;
        rootInterprter = new Interpreter(this, program.main, new LocalStack());
    }

    public void addStartStopListener(StartStopListener startStopListener) {
        startStopListeners.add(startStopListener);
    }


    public boolean isRunning() {
        return interpreterThread != null;
    }

    public void terminate() {
        if (interpreterThread != null) {
            interpreterThread.interrupt();
            interpreterThread = null;
            for (StartStopListener startStopListener : startStopListeners) {
                startStopListener.programStopped();
            }
        }
    }

    public void runAsync(final Runnable runnable) {
        terminate();
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


    public void runAsync() {
        program.clear(rootInterprter);
        stopped = false;
        runAsync(0);
    }


    // Called from the shell
    public void runStatementsAsync(final List<? extends Node> statements, final ProgramControl programInterpreterControl) {
        runAsync(new Runnable() {
            @Override
            public void run() {
                rootInterprter.currentLine = -2;
                Object result = rootInterprter.runStatementsImpl(statements);
                if (rootInterprter.currentLine >= 0) {
                    programInterpreterControl.runAsync(rootInterprter.currentLine);
                } else if (statements.size() == 0 || (!(statements.get(statements.size() - 1) instanceof IoStatement)
                        || ((IoStatement) statements.get(statements.size() - 1)).kind != IoStatement.Kind.PRINT)) {
                    program.console.print(result == null ? "OK\n" : (String.valueOf(result) + "\n"));
                }

            }
        });
    }



    public void runAsync(final int runLine) {
        runAsync(new Runnable() {
            @Override
            public void run() {
                rootInterprter.currentLine = runLine;
                rootInterprter.runCallableUnit();
            }
        });
    }

    public void setPaused(boolean paused) {
        this.stopped = paused;
    }

    public void setTrace(boolean trace) {
        this.trace = trace;
    }
}
