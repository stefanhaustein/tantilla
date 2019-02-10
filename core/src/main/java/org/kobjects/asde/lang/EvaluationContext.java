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

    public EvaluationContext(ProgramControl control, FunctionImplementation functionImplementation, LocalStack localStack) {
        this.control = control;
        this.functionImplementation = functionImplementation;
        this.localStack = localStack;
    }


    public GlobalSymbol.Scope getSymbolScope() {
        return currentLine == -2 ? GlobalSymbol.Scope.PERSISTENT : GlobalSymbol.Scope.TRANSIENT;
    }

}
