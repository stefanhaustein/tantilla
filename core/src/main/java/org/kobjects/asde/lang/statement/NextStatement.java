package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.JumpStackEntry;
import org.kobjects.asde.lang.type.Types;
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
    public Object eval(EvaluationContext evaluationContext) {
        JumpStackEntry entry;
        while (true) {
            if (evaluationContext.stack.isEmpty()
                    || evaluationContext.stack.get(evaluationContext.stack.size() - 1).forVariable == null) {
                throw new RuntimeException("NEXT " + varName+ " without FOR.");
            }
            entry = evaluationContext.stack.remove(evaluationContext.stack.size() - 1);
            if (varName == null || entry.forVariableName.equals(varName)) {
                break;
            }
        }
        double current = ((Double) entry.forVariable.get(evaluationContext)) + entry.step;
        entry.forVariable.set(evaluationContext, current);
        if (Math.signum(entry.step) != Math.signum(Double.compare(current, entry.end))) {
            evaluationContext.stack.add(entry);
            evaluationContext.currentLine = entry.lineNumber;
            evaluationContext.currentIndex = entry.statementIndex + 1;
            return null;
        }
        evaluationContext.nextSubIndex = 0;
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
