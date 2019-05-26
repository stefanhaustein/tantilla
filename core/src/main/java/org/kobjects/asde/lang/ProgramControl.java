package org.kobjects.asde.lang;

import org.kobjects.asde.lang.event.StartStopListener;
import org.kobjects.asde.lang.type.CodeLine;

import java.util.ArrayList;
import java.util.List;

public class ProgramControl {
    Thread interpreterThread;
    private final List<StartStopListener> startStopListeners = new ArrayList<>();
    public Program program;

    public enum State {
        PAUSED, ABORTING, ABORTED, ENDED, RUNNING, STEP
    }

    State state = State.ENDED;
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
        program.clear(context);
        runAsync(() -> context.function.callImpl(context));
    }


    public static Object runCodeLineImpl(CodeLine codeLine, EvaluationContext evaluationContext) {
        int line = evaluationContext.currentLine;
        Object result = null;
        while (evaluationContext.currentIndex < codeLine.length() && evaluationContext.control.state != State.ABORTING) {
            int index = evaluationContext.currentIndex;
            result = codeLine.get(index).eval(evaluationContext);
            if (evaluationContext.currentLine != line) {
                return result;  // Goto or similar out of the current line
            }
            if (evaluationContext.currentIndex == index) {
                evaluationContext.currentIndex++;
            }
        }
        evaluationContext.currentIndex = 0;
        evaluationContext.currentLine++;
        return result;
    }

    // Called from the shell
    public void runStatementsAsync(FunctionImplementation wrapper, final ProgramControl programInterpreterControl, Consumer resultConsumer) {
        runAsync(() -> {
            EvaluationContext wrapperContext = new EvaluationContext(ProgramControl.this, wrapper);
            wrapperContext.currentLine = -2;
                Object result = runCodeLineImpl(wrapper.entrySet().iterator().next().getValue(), wrapperContext);
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
