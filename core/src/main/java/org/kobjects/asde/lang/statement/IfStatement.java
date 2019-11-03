package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.StatementMatcher;
import org.kobjects.asde.lang.type.CodeLine;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class IfStatement extends Node {
    public final boolean multiline;
    public final boolean elseIf;

    int resolvedLine;
    int resolvedIndex;

    public IfStatement(Node condition, boolean multiline, boolean elseIf) {
        super(condition);
        this.multiline = multiline;
        this.elseIf = elseIf;
    }

    @Override
    protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
        if (!Types.match(children[0].returnType(), Types.BOOLEAN) &&
                !Types.match(children[0].returnType(), Types.NUMBER)) {
            throw new RuntimeException("Boolean condition value expected.");
        }
        if (multiline && !elseIf) {
            resolutionContext.startBlock(FunctionValidationContext.BlockType.IF);
        }

        if (multiline) {
            EndifMatcher matcher = new EndifMatcher();
            int[] pos = new int[] {line + 1, 0};
            resolutionContext.functionImplementation.find(matcher, pos);
            resolvedLine = pos[0];
            resolvedIndex = pos[1] + 1;
        } else {
            CodeLine codeLine = resolutionContext.functionImplementation.findNextLine(line);
            while (++index < codeLine.length()) {
                if (codeLine.get(index) instanceof ElseStatement) {
                    break;
                }
            }
            resolvedIndex = index;
            resolvedLine = line;
        }
    }

    @Override
    public Object eval(EvaluationContext evaluationContext) {
      if (!children[0].evalBoolean(evaluationContext)) {
            evaluationContext.currentLine = resolvedLine;
            evaluationContext.currentIndex = resolvedIndex;
        }
        return null;
    }

    @Override
    public Type returnType() {
        return Types.VOID;
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
        appendLinked(asb, "IF ", errors);
        children[0].toString(asb, errors, preferAscii);
        asb.append(" THEN");
    }

    static class EndifMatcher implements StatementMatcher {
        int skip;

        @Override
        public boolean statementMatches(CodeLine line, int index, Node statement) {
            if (statement instanceof IfStatement && ((IfStatement) statement).multiline && !((IfStatement) statement).elseIf) {
                skip++;
            } else if (statement instanceof ElseStatement && ((ElseStatement) statement).multiline && skip == 0) {
               return true;
            } else if (statement instanceof EndIfStatement) {
              if (skip == 0) {
                 return true;
              }
              skip--;
            }
            return false;
        }
    }
}
