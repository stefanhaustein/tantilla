package org.kobjects.asde.lang.runtime;

import org.kobjects.asde.lang.classifier.InstanceImpl;
import org.kobjects.asde.lang.function.FunctionImplementation;
import org.kobjects.asde.lang.program.GlobalSymbol;
import org.kobjects.asde.lang.program.ProgramControl;

import java.util.ArrayList;

public class EvaluationContext {
    public final ProgramControl control;
    public final FunctionImplementation function;

    private final DataStack dataStack;
    private final int stackBase;
    public final InstanceImpl self;
    private int stackTop;

    public Object returnValue;

    public int currentLine;
    public int currentIndex;


    /**
     * Creates a new root evaluation context.
     */
    public EvaluationContext(ProgramControl control, FunctionImplementation function) {
        this.control = control;
        this.function = function;
        stackBase = 0;
        stackTop = function.localVariableCount;
        dataStack = new DataStack(stackTop);
        self = null;

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
        currentIndex = parentContext.currentIndex;
        self = parentContext.self;
        System.arraycopy(parentContext.dataStack.data, parentContext.stackBase, dataStack.data, stackBase, stackTop);

        control.lastCreatedContext = this;
    }

    /**
     * Creates a new context for calling the given function.
     */
    public EvaluationContext(EvaluationContext parentContext, FunctionImplementation functionImplementation, InstanceImpl self) {
        this.function = functionImplementation;
        this.self = self;
        this.control = parentContext.control;
        this.dataStack = parentContext.dataStack;
        this.stackBase = parentContext.stackTop;
        this.stackTop = stackBase + functionImplementation.localVariableCount;

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
