package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.type.CodeLine;

import java.util.Map;

public class EndStatement extends Statement {
    @Override
    protected void onResolve(FunctionValidationContext resolutionContext, int line, int index) {
        CodeLine codeLine = resolutionContext.functionImplementation.ceilingEntry(line).getValue();
        if (codeLine.length() > 1) {
            throw new RuntimeException("END must be on a separate line.");
        }
    }

    @Override
    public Object eval(EvaluationContext evaluationContext) {
        evaluationContext.currentLine = Integer.MAX_VALUE;
        return null;
    }


    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
        appendLinked(asb, "END", errors);
    }
}
