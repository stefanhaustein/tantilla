package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.asde.lang.function.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.runtime.EvaluationContext;

import java.util.Map;

public class ConditionStatement extends BlockStatement {

    public final Kind kind;

    public enum Kind {
        IF, ELIF, ELSE
    }

    int resolvedEndLine;
    int resolvedLine;
    ConditionStatement resolvedPrevious;
    ConditionStatement resolvedNext;

    public ConditionStatement(Kind kind, Node condition) {
        super(condition);
        this.kind = kind;
    }

    @Override
    public boolean closesBlock() {
        return kind != Kind.IF;
    }


    @Override
    protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line) {
        resolvedLine = line;
        resolvedPrevious = null;
        resolvedNext = null;
        resolvedEndLine = Integer.MAX_VALUE;

        if (kind == Kind.IF) {
            resolvedPrevious = null;
        } else {
            if (!(resolutionContext.getCurrentBlock().startStatement instanceof ConditionStatement)) {
                throw new RuntimeException("The block start must be 'if' or 'elif', but was: " + resolutionContext.getCurrentBlock().startStatement);
            }
            resolvedPrevious = (ConditionStatement) resolutionContext.getCurrentBlock().startStatement;
            if (resolvedPrevious.kind == Kind.ELSE) {
                throw new RuntimeException("The block start must be 'if' or 'elif' for '" + kind.name().toLowerCase() + "' + but was 'else'.");
            }
            resolutionContext.endBlock(this, line);
        }
        if (children[0].returnType()!= Types.BOOLEAN) {
            throw new RuntimeException("Boolean condition value expected.");
        }
        resolutionContext.startBlock(this, line);
    }


    @Override
    public Object eval(EvaluationContext evaluationContext) {
        if (kind == Kind.IF) {
            ConditionStatement current = this;
            while (!current.children[0].evalBoolean(evaluationContext)) {
                current = current.resolvedNext;
                if (current == null) {
                    evaluationContext.currentLine = resolvedEndLine;
                    return null;
                }
            }
            evaluationContext.currentLine = current.resolvedLine + 1;
        } else {
            evaluationContext.currentLine = resolvedEndLine;
        }
        return null;
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
        appendLinked(asb, kind.name().toLowerCase(), errors);
        asb.append(' ');
        if (kind != Kind.ELSE) {
            children[0].toString(asb, errors, preferAscii);
        }
        asb.append(":");
    }

    @Override
    public void onResolveEnd(FunctionValidationContext resolutionContext, Node endStatement, int endLine) {
        if (endStatement instanceof ConditionStatement) {
            resolvedNext = (ConditionStatement) endStatement;
        } else {
            ConditionStatement current = this;
            do {
                resolvedEndLine = endLine;
                current = current.resolvedPrevious;
            } while (current != null);
        }
    }

    @Override
    void evalEnd(EvaluationContext context) {
        // Nothing to do
    }
}
