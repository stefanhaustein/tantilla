package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.JumpStackEntry;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.asde.lang.ResolvedSymbol;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class ForStatement extends Node {
    private final String varName;
    ResolvedSymbol resolved;

    public ForStatement(String varName, Node... children) {
        super(children);
        this.varName = varName;
    }

    public void onResolve(FunctionValidationContext resolutionContext, int line, int index) {
        resolutionContext.startBlock(FunctionValidationContext.BlockType.FOR);
        // TODO: Check types?
        resolved = resolutionContext.declare(varName, Types.NUMBER, false);
    }

    @Override
    public Object eval(EvaluationContext evaluationContext) {
            double current = evalChildToDouble(evaluationContext,0);
            resolved.set(evaluationContext, current);
            double end = evalChildToDouble(evaluationContext, 1);
            double step = children.length > 2 ? evalChildToDouble(evaluationContext, 2) : 1.0;
            if (Math.signum(step) == Math.signum(Double.compare(current, end))) {
                int nextPosition[] = new int[3];
                if (evaluationContext.function.find((Node statement) -> (statement instanceof NextStatement
                        && (((NextStatement) statement).varName == null || ((NextStatement) statement).varName.equals(varName))), nextPosition) == null) {
                    throw new RuntimeException("FOR without NEXT");
                }
                evaluationContext.currentLine = nextPosition[0];
                evaluationContext.currentIndex = nextPosition[1];
                evaluationContext.nextSubIndex = nextPosition[2] + 1;
            } else {
                JumpStackEntry entry = new JumpStackEntry();
                entry.forVariable = resolved;
                entry.forVariableName = varName;
                entry.end = end;
                entry.step = step;
                entry.lineNumber = evaluationContext.currentLine;
                entry.statementIndex = evaluationContext.currentIndex;
                evaluationContext.getJumpStack().add(entry);
            }
            return null;
    }

    @Override
    public Type returnType() {
        return Types.VOID;
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
        appendLinked(asb, "FOR " + varName + " = ", errors);
        children[0].toString(asb, errors);
        asb.append(" TO ");
        children[1].toString(asb, errors);
        if (children.length > 2) {
            asb.append(" STEP ");
            children[2].toString(asb, errors);
        }
    }
}
