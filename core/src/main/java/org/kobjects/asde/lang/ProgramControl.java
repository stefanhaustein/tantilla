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

    public enum State {
        PAUSED, TERMINATING, TERMINATED, RUNNING,
    }

    State state = State.TERMINATED;
    boolean trace;

    public ProgramControl(Program program) {
        this.program = program;
        rootInterprter = new Interpreter(this, program.main, new LocalStack());
    }

    public void addStartStopListener(StartStopListener startStopListener) {
        startStopListeners.add(startStopListener);
    }


    public State getState() {
        return state;
    }

    public synchronized void terminate() {
        if (state == State.TERMINATED || state == State.TERMINATING) {
           return;
        }
        state = State.TERMINATING;
        if (interpreterThread != null) {
          interpreterThread.interrupt();
        }
    }

    private void runAsync(final Runnable runnable) {
        if (state != State.TERMINATED) {
            throw new IllegalStateException("Can't start in state " + state);
        }
        interpreterThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception e) {
                    e.printStackTrace();
                    program.console.print(e.toString());
                }
                state = State.TERMINATED;
                for (StartStopListener startStopListener : startStopListeners) {
                   startStopListener.programTerminated();
                }
            }
        });
        state = State.RUNNING;
        for (StartStopListener startStopListener : startStopListeners) {
            startStopListener.programStarted();
        }
        interpreterThread.start();
    }


    public void start() {
        program.clear(rootInterprter);
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

    private void runAsync(final int runLine) {
        runAsync(new Runnable() {
            @Override
            public void run() {
                rootInterprter.currentLine = runLine;
                rootInterprter.runCallableUnit();
            }
        });
    }

    public synchronized void pause() {
        if (state != State.RUNNING) {
           throw new IllegalStateException("Can't pause in state " + state);
        }
        this.state = State.PAUSED;
        for (StartStopListener startStopListener : startStopListeners) {
           startStopListener.programPaused();
        }
    }

    public synchronized void resume() {
        if (state != State.PAUSED) {
            throw new IllegalStateException("Can't resume in state " + state);
        }
        this.state = State.RUNNING;
        for (StartStopListener startStopListener : startStopListeners) {
            startStopListener.programStarted();
        }
    }

    public void setTrace(boolean trace) {
        this.trace = trace;
    }
}
