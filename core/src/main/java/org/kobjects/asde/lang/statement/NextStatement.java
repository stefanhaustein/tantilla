package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.StackEntry;
import org.kobjects.asde.lang.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class NextStatement extends Node {

    final String varName;

    public NextStatement(String varName) {
        this.varName = varName;
    }

    @Override
    public Object eval(Interpreter interpreter) {
        StackEntry entry;
        while (true) {
            if (interpreter.stack.isEmpty()
                    || interpreter.stack.get(interpreter.stack.size() - 1).forVariable == null) {
                throw new RuntimeException("NEXT " + varName+ " without FOR.");
            }
            entry = interpreter.stack.remove(interpreter.stack.size() - 1);
            if (varName == null || entry.forVariableName.equals(varName)) {
                break;
            }
        }
        double current = ((Double) entry.forVariable.get(interpreter)) + entry.step;
        entry.forVariable.set(interpreter, current);
        if (Math.signum(entry.step) != Math.signum(Double.compare(current, entry.end))) {
            interpreter.stack.add(entry);
            interpreter.currentLine = entry.lineNumber;
            interpreter.currentIndex = entry.statementIndex + 1;
            return null;
        }
        interpreter.nextSubIndex = 0;
        return null;
    }

    @Override
    public Type returnType() {
        return Types.VOID;
    }

    public void onResolve(FunctionValidationContext resolutionContext, int line, int index) {
        resolutionContext.endBlock(FunctionValidationContext.BlockType.FOR);
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
        if (varName == null) {
            appendLinked(asb, "NEXT", errors);
        } else {
            appendLinked(asb, "NEXT " + varName, errors);
        }
    }
}
