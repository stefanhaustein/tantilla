package org.kobjects.asde.lang;

import org.kobjects.asde.lang.node.Node;

import java.util.ArrayList;
import java.util.List;

public class ProgramControl {
    Thread interpreterThread;
    private final List<StartStopListener> startStopListeners = new ArrayList<>();
    public Program program;
    Interpreter rootInterprter;

    public enum State {
        PAUSED, ABORTING, ABORTED, ENDED, RUNNING, STEP
    }

    State state = State.ENDED;
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

    public synchronized void abort() {
        if (state == State.ENDED || state == State.ABORTING || state == State.ABORTED) {
           return;
        }
        state = State.ABORTING;
        if (interpreterThread != null) {
          interpreterThread.interrupt();
        }
    }

    private void runAsync(final Runnable runnable) {
        if (state != State.ENDED && state != State.ABORTED) {
            throw new IllegalStateException("Can't start in state " + state);
        }
        interpreterThread = new Thread(() -> {
                try {
                    runnable.run();
                } catch (Exception e) {
                    e.printStackTrace();
                    program.console.print(e.toString());
                }
                boolean aborted = state == State.ABORTING;
                state = aborted ? State.ABORTED : State.ENDED;
                for (StartStopListener startStopListener : startStopListeners) {
                   if (aborted) {
                       startStopListener.programAborted();
                   } else {
                       startStopListener.programEnded();
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
    public void runStatementsAsync(final List<? extends Node> statements, final ProgramControl programInterpreterControl, Consumer resultConsumer) {
        runAsync(() -> {
                rootInterprter.currentLine = -2;
                Object result = rootInterprter.runStatementsImpl(statements);
                if (rootInterprter.currentLine >= 0) {
                    programInterpreterControl.runAsync(rootInterprter.currentLine);
                } else {
                    resultConsumer.accept(result);
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

    public synchronized void step() {
        if (state != State.PAUSED) {
            throw new IllegalStateException("Can't step in state " + state);
        }
        this.state = State.STEP;
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
