package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.CallableUnit;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.parser.ResolutionContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class ElseStatement extends Node {

    public final boolean multiline;

    public ElseStatement(boolean multiline) {
        this.multiline = multiline;
    }

    @Override
    protected void onResolve(ResolutionContext resolutionContext) {
        resolutionContext.endBlock(ResolutionContext.BlockType.IF);
        resolutionContext.startBlock(ResolutionContext.BlockType.IF);
    }

    @Override
    public Object eval(Interpreter interpreter) {
        if (multiline) {
            EndifMatcher matcher = new EndifMatcher();
            int[] pos = new int[] {interpreter.currentLine + 1, 0};
            interpreter.callableUnit.find(matcher, pos);
            interpreter.currentLine = pos[0];
            interpreter.currentIndex = pos[1] + 1;
        } else {
            interpreter.currentIndex = 0;
            interpreter.currentLine++;
        }
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