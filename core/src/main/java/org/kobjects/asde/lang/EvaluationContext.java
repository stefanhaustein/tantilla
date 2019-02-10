package org.kobjects.asde.lang;

import org.kobjects.asde.lang.statement.LegacyStatement;

import java.util.ArrayList;

public class EvaluationContext {
    public final FunctionImplementation functionImplementation;
    public int currentLine;
    public int currentIndex;
    public int nextSubIndex;  // index within next when skipping a for loop; reset in next
    public ArrayList<JumpStackEntry> stack = new ArrayList<>();

    public int[] dataPosition = new int[3];
    public LegacyStatement dataStatement;
    public Object returnValue;
    public final LocalStack localStack;
    public final ProgramControl control;
    private final int stackBase;
    private int stackTop;



    public EvaluationContext(ProgramControl control, FunctionImplementation functionImplementation) {
        this.control = control;
        this.functionImplementation = functionImplementation;
        this.stackBase = 0;
        this.stackTop = functionImplementation.localVariableCount;
        this.localStack = new LocalStack(stackTop);
    }


    public EvaluationContext(EvaluationContext parentContext, FunctionImplementation functionImplementation) {
        this.functionImplementation = functionImplementation;
        this.control = parentContext.control;
        this.localStack = parentContext.localStack;
        this.stackBase = parentContext.stackTop;
        this.stackTop = stackBase + functionImplementation.localVariableCount;
    }

    public Object getLocal(int index) {
        return localStack.get(stackBase + index);
    }

    public void setLocal(int index, Object value) {
        localStack.set(stackBase + index, value);
    }

    public Object getParameter(int index) {
        return localStack.get(stackTop + index);
    }

    public GlobalSymbol.Scope getSymbolScope() {
        return currentLine == -2 ? GlobalSymbol.Scope.PERSISTENT : GlobalSymbol.Scope.TRANSIENT;
    }

    public void ensureExtraStackSpace(int localVariableCount) {
        localStack.ensureSize(stackTop + localVariableCount);
    }

    public void push(Object value) {
        localStack.set(stackTop++, value);
    }

    public void popN(int count) {
        stackTop -= count;
    }
}
