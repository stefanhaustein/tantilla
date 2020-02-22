package org.kobjects.asde.lang.program;

import org.kobjects.asde.lang.Consumer;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.runtime.StartStopListener;
import org.kobjects.asde.lang.function.UserFunction;
import org.kobjects.asde.lang.statement.Statement;

import java.util.ArrayList;
import java.util.List;

public class ProgramControl {
    Thread interpreterThread;
    public final List<StartStopListener> startStopListeners = new ArrayList<>();
    public Program program;

    // For debugger until we have something per thread.
    public EvaluationContext lastCreatedContext;


    public enum State {
        PAUSED, ABORTING, ABORTED, ENDED, RUNNING, STEP
    }

    public State state = State.ENDED;
    boolean trace;

    public ProgramControl(Program program) {
        this.program = program;
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
            Exception exception = null;
                try {
                    runnable.run();
                } catch (Exception e) {
                    exception = e;
                    state = state.ABORTING;
                }
                boolean aborted = state == State.ABORTING;
                state = aborted ? State.ABORTED : State.ENDED;
                for (StartStopListener startStopListener : startStopListeners) {
                   if (aborted) {
                       startStopListener.programAborted(exception);
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
        EvaluationContext context = new EvaluationContext(this, program.main);
        try {
            program.clear(context);
            runAsync(() -> context.function.callImpl(context));
        } catch (Exception e) {
            program.console.showError("Error starting program", e);
        }
    }


    public static Object runCodeLineImpl(Statement statement, EvaluationContext evaluationContext) {
        int line = evaluationContext.currentLine;
        Object result = statement.eval(evaluationContext);
        if (evaluationContext.currentLine != line) {
          return result;  // Goto or similar out of the current line
        }
        evaluationContext.currentLine++;
        return result;
    }

    // Called from the shell
    public void runStatementsAsync(UserFunction wrapper, final ProgramControl programInterpreterControl, Consumer resultConsumer) {
        runAsync(() -> {
            EvaluationContext wrapperContext = new EvaluationContext(ProgramControl.this, wrapper);
            wrapperContext.currentLine = -2;
                Object result = runCodeLineImpl(wrapper.allLines().iterator().next(), wrapperContext);
                if (wrapperContext.currentLine >= 0) {
                    programInterpreterControl.runAsync(wrapperContext.currentLine);
                } else {
                    resultConsumer.accept(result);
                }
        });
    }

    private void runAsync(final int runLine) {
        runAsync(new Runnable() {
            @Override
            public void run() {
                EvaluationContext context = new EvaluationContext(ProgramControl.this, program.main);
                context.currentLine = runLine;
                program.main.callImpl(context);
            }
        });
    }

    public synchronized void pause() {
        if (state != State.RUNNING) {
           throw new IllegalStateException("Can't pause in state " + state);
        }
        this.state = State.STEP;
    }

    public synchronized void step() {
        if (state != State.PAUSED) {
            throw new IllegalStateException("Can't step in state " + state);
        }
        this.state = State.STEP;
    }

    public synchronized void resume() {
        if (state != State.PAUSED && state != State.STEP) {
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
