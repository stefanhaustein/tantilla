package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.CallableUnit;
import org.kobjects.asde.lang.CodeLine;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Types;
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
    protected void onResolve(FunctionValidationContext resolutionContext, int line, int index) {
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
            resolutionContext.callableUnit.find(matcher, pos);
            resolvedLine = pos[0];
            resolvedIndex = pos[1] + 1;
        } else {
            CodeLine codeLine = resolutionContext.callableUnit.ceilingEntry(line).getValue();
            while (++index < codeLine.statements.size()) {
                if (codeLine.statements.get(index) instanceof ElseStatement) {
                    break;
                }
            }
            resolvedIndex = index;
            resolvedLine = line;
        }
    }

    @Override
    public Object eval(Interpreter interpreter) {
        if (!evalChildToBoolean(interpreter, 0)) {
            interpreter.currentLine = resolvedLine;
            interpreter.currentIndex = resolvedIndex;
        }
        return null;
    }

    @Override
    public Type returnType() {
        return Types.VOID;
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
        appendLinked(asb, "IF ", errors);
        children[0].toString(asb, errors);
        asb.append(" THEN");
    }

    static class EndifMatcher implements CallableUnit.StatementMatcher {
        int skip;

        @Override
        public boolean statementMatches(Node statement) {
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
