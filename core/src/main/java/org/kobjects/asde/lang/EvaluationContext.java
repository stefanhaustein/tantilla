package org.kobjects.asde.lang;

import org.kobjects.asde.lang.statement.LegacyStatement;

import java.util.ArrayList;

public class EvaluationContext {
    public final ProgramControl control;
    public final FunctionImplementation function;

    private final DataStack dataStack;
    private final int stackBase;
    private int stackTop;

    public Object returnValue;

    public int currentLine;
    public int currentIndex;
    public int nextSubIndex;  // index within next when skipping a for loop; reset in next

    private ArrayList<JumpStackEntry> jumpStack;

    private int[] dataPosition;
    public LegacyStatement dataStatement;


    /**
     * Creates a new root evaluation context.
     */
    public EvaluationContext(ProgramControl control, FunctionImplementation function) {
        this.control = control;
        this.function = function;
        stackBase = 0;
        stackTop = function.localVariableCount;
        dataStack = new DataStack(stackTop);
    }

    /**
     * Creates a copy of the given context for forking threads.
     */
    public EvaluationContext(EvaluationContext parentContext) {
        control = parentContext.control;
        function = parentContext.function;
        stackBase = 0;
        stackTop = parentContext.stackTop - parentContext.stackBase;
        dataStack = new DataStack(stackTop);
        currentLine = parentContext.currentLine;
        currentIndex = parentContext.currentIndex;
        System.arraycopy(parentContext.dataStack.data, parentContext.stackBase, dataStack.data, stackBase, stackTop);
    }

    /**
     * Creates a new context for calling the given function.
     */
    public EvaluationContext(EvaluationContext parentContext, FunctionImplementation functionImplementation) {
        this.function = functionImplementation;
        this.control = parentContext.control;
        this.dataStack = parentContext.dataStack;
        this.stackBase = parentContext.stackTop;
        this.stackTop = stackBase + functionImplementation.localVariableCount;
    }

    public int[] getDataPosition() {
        if (dataPosition == null) {
            dataPosition = new int[3];
        }
        return dataPosition;
    }

    public ArrayList<JumpStackEntry> getJumpStack() {
        if (jumpStack == null) {
            jumpStack = new ArrayList<>();
        }
        return jumpStack;
    }


    public Object getLocal(int index) {
        return dataStack.data[stackBase + index];
    }

    public void setLocal(int index, Object value) {
        dataStack.data[stackBase + index] = value;
    }

    public Object getParameter(int index) {
        return dataStack.data[stackTop + index];
    }

    public GlobalSymbol.Scope getSymbolScope() {
        return currentLine == -2 ? GlobalSymbol.Scope.PERSISTENT : GlobalSymbol.Scope.TRANSIENT;
    }

    public void ensureExtraStackSpace(int localVariableCount) {
        dataStack.ensureSize(stackTop + localVariableCount);
    }

    public void push(Object value) {
        dataStack.data[stackTop++] = value;
    }

    public void popN(int count) {
        stackTop -= count;
    }

    static class DataStack {
        Object[] data;
        int limit;

        DataStack(int initialSize) {
            this.data = new Object[initialSize];
            limit = initialSize;
        }

        void ensureSize(int i) {
            limit = i;
            if (data.length < i) {
                Object[] newStack = new Object[Math.max(i, data.length * 3 / 2)];
                System.arraycopy(data, 0, newStack, 0, data.length);
                data = newStack;
            }
        }
    }


}
