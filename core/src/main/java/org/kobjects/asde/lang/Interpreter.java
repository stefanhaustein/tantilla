package org.kobjects.asde.lang;

import org.kobjects.asde.lang.node.Statement;

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
    public Interpreter(Program program) {this.program = program;}

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

    public void runProgram() {
        runStatements(Collections.singletonList(new Statement(program, Statement.Type.RUN)));
    }

    public void runStatements(final List<Statement> statements) {
        stop();
        interpreterThread = new Thread(new Runnable() {
            @Override
            public void run() {
                currentLine = -2;
                runStatementsImpl(statements);
                if (currentLine > -1) {
                    Map.Entry<Integer, List<Statement>> entry;
                    while (null != (entry = program.main.code.ceilingEntry(currentLine)) && !Thread.currentThread().isInterrupted()) {
                        currentLine = entry.getKey();
                        runStatementsImpl(entry.getValue());
                    }
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

}
