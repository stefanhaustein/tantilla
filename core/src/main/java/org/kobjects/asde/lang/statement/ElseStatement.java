package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.type.CallableUnit;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class ElseStatement extends Node {

    public final boolean multiline;

    public int resolvedLine;
    public int resolvedIndex;

    public ElseStatement(boolean multiline) {
        this.multiline = multiline;
    }

    @Override
    protected void onResolve(FunctionValidationContext resolutionContext, int line, int index) {
        resolutionContext.endBlock(FunctionValidationContext.BlockType.IF);
        resolutionContext.startBlock(FunctionValidationContext.BlockType.IF);
        if (multiline) {
            EndifMatcher matcher = new EndifMatcher();
            int[] pos = new int[] {line + 1, 0};
            resolutionContext.callableUnit.find(matcher, pos);
            resolvedLine = pos[0];
            resolvedIndex = pos[1] + 1;
        } else {
            resolvedIndex = 0;
            resolvedLine = line + 1;
        }
    }

    @Override
    public Object eval(Interpreter interpreter) {
        interpreter.currentLine = resolvedLine;
        interpreter.currentIndex = resolvedIndex;
        return null;
    }

    @Override
    public Type returnType() {
        return null;
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
        appendLinked(asb, "ELSE", errors);
    }


    /**
     * Else is reached only after a successful if and hence always goes to the end
     */
    static class EndifMatcher implements CallableUnit.StatementMatcher {
        int level;

        @Override
        public boolean statementMatches(Node statement) {
            if (statement instanceof IfStatement && ((IfStatement) statement).multiline && !((IfStatement) statement).elseIf) {
                level++;
            } else if (statement instanceof EndIfStatement) {
                if (level == 0) {
                    return true;
                }
                level--;
            }
            return false;
        }
    }
}