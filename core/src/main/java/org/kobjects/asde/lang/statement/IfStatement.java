package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.CallableUnit;
import org.kobjects.asde.lang.CodeLine;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.parser.ResolutionContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class IfStatement extends Node {
    public final boolean multiline;
    public final boolean elseIf;

    public IfStatement(Node condition, boolean multiline, boolean elseIf) {
        super(condition);
        this.multiline = multiline;
        this.elseIf = elseIf;
    }

    @Override
    protected void onResolve(ResolutionContext resolutionContext) {
        if (!Types.match(children[0].returnType(), Types.BOOLEAN) &&
                !Types.match(children[0].returnType(), Types.NUMBER)) {
            throw new RuntimeException("Boolean condition value expected.");
        }
        if (multiline && !elseIf) {
            resolutionContext.startBlock(ResolutionContext.BlockType.IF);
        }
    }

    @Override
    public Object eval(Interpreter interpreter) {
        if (!evalChildToBoolean(interpreter, 0)) {
            if (multiline) {
                EndifMatcher matcher = new EndifMatcher();
                int[] pos = new int[] {interpreter.currentLine + 1, 0};
                interpreter.callableUnit.find(matcher, pos);
                interpreter.currentLine = pos[0];
                interpreter.currentIndex = pos[1] + 1;
            } else {
                CodeLine line = interpreter.callableUnit.ceilingEntry(interpreter.currentLine).getValue();
                while (++interpreter.currentIndex < line.statements.size()) {
                    if (line.statements.get(interpreter.currentIndex) instanceof ElseStatement) {
                        break;
                    }
                }
            }
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
