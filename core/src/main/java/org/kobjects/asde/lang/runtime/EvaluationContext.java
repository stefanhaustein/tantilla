package org.kobjects.asde.lang.runtime;

import org.kobjects.asde.lang.function.Callable;
import org.kobjects.asde.lang.function.UserFunction;
import org.kobjects.asde.lang.program.ProgramControl;

public class EvaluationContext {
    public final ProgramControl control;
    public final UserFunction function;

    public final DataArray dataStack;
    public int stackBase;

    public Object returnValue;

    public int currentLine = 1;


    /**
     * Creates a new root evaluation context.
     */
    public EvaluationContext(ProgramControl control, UserFunction function) {
        this.control = control;
        this.function = function;
        stackBase = 0;
        dataStack = new DataArray(function.localVariableCount);

        control.lastCreatedContext = this;
    }

    /**
     * Creates a copy of the given context for forking threads.
     */
    public EvaluationContext(EvaluationContext parentContext) {
        control = parentContext.control;
        function = parentContext.function;
        stackBase = 0;

        dataStack = new DataArray(parentContext.dataStack.size() - parentContext.stackBase);
        currentLine = parentContext.currentLine;
        System.arraycopy(parentContext.dataStack.objects, parentContext.stackBase, dataStack.objects, stackBase, dataStack.size);
        System.arraycopy(parentContext.dataStack.numbers, parentContext.stackBase, dataStack.numbers, stackBase, dataStack.size);

        control.lastCreatedContext = this;
    }

    /**
     * Creates a new context for calling the given function.
     */
    public EvaluationContext(EvaluationContext parentContext, UserFunction userFunction) {
        this.function = userFunction;
        this.control = parentContext.control;
        this.dataStack = parentContext.dataStack;
        this.stackBase = parentContext.stackBase;

        control.lastCreatedContext = this;
    }

    public Object getLocal(int index) {
        return dataStack.getObject(stackBase + index);
    }

    public void setLocal(int index, Object value) {
        dataStack.setObject(stackBase + index, value);
    }

    public Object getParameter(int index) {
        return dataStack.getObject(stackBase + index);
    }


    public Object call(Callable callable, int paramCount) {
        int savedStackSize = dataStack.size();
        int savedStackBase = stackBase;
        stackBase = savedStackSize - paramCount;
        try {
            dataStack.ensureSize(stackBase + callable.getLocalVariableCount());
            Object result = callable.call(this, paramCount);
            if (dataStack.size() != stackBase + callable.getLocalVariableCount()) {
                throw new RuntimeException("Expected stack size " + savedStackSize + "; actual: " + dataStack.size() + " in " + callable);
            }
            return result;
        } finally {
            dataStack.popN(dataStack.size() - savedStackSize + paramCount);
            stackBase = savedStackBase;
        }
    }

    public void push(Object value) {
        if (value == null) {
            throw new NullPointerException();
        }
        dataStack.pushObject(value);
    }

    public Object pop() {
        return dataStack.popObject();
    }


    public double popDouble() {
        return (Double) pop();
    }

    public float popFloat() {
        return (Float) pop();
    }

    public int popInt() {
        return (Integer) pop();
    }

    public long popLong() {
        return (Long) pop();
    }

    public boolean popBoolean() {
        return (Boolean) pop();
    }


}
