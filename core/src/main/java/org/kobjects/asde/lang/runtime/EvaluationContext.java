package org.kobjects.asde.lang.runtime;

import org.kobjects.asde.lang.classifier.Instance;
import org.kobjects.asde.lang.function.UserFunction;
import org.kobjects.asde.lang.program.GlobalSymbol;
import org.kobjects.asde.lang.program.ProgramControl;

public class EvaluationContext {
    public final ProgramControl control;
    public final UserFunction function;

    private final DataStack dataStack;
    private final int stackBase;
    private int stackTop;

    public Object returnValue;

    public int currentLine = 1;


    /**
     * Creates a new root evaluation context.
     */
    public EvaluationContext(ProgramControl control, UserFunction function) {
        this.control = control;
        this.function = function;
        stackBase = 0;
        stackTop = function.localVariableCount;
        dataStack = new DataStack(stackTop);

        control.lastCreatedContext = this;
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
        System.arraycopy(parentContext.dataStack.data, parentContext.stackBase, dataStack.data, stackBase, stackTop);

        control.lastCreatedContext = this;
    }

    /**
     * Creates a new context for calling the given function.
     */
    public EvaluationContext(EvaluationContext parentContext, UserFunction userFunction, Instance self) {
        this.function = userFunction;
        this.control = parentContext.control;
        this.dataStack = parentContext.dataStack;
        this.stackBase = parentContext.stackTop;
        this.stackTop = stackBase + userFunction.localVariableCount;

        control.lastCreatedContext = this;
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
