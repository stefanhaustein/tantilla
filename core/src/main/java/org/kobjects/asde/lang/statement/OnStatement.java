package org.kobjects.asde.lang.statement;

import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.FunctionImplementation;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.asde.lang.node.Identifier;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.node.Path;
import org.kobjects.asde.lang.node.Visitor;
import org.kobjects.asde.lang.type.CodeLine;
import org.kobjects.typesystem.Property;
import org.kobjects.typesystem.PropertyChangeListener;

public class OnStatement extends Statement implements PropertyChangeListener {

    EvaluationContext context;
    int lineBeyondEnd;

    public OnStatement(Node condition) {
        super(condition);
    }

    @Override
    protected void onResolve(FunctionValidationContext resolutionContext, int line, int index) {
        CodeLine codeLine = resolutionContext.functionImplementation.ceilingEntry(line).getValue();
        if (codeLine.length() > index + 1) {
            lineBeyondEnd = line + 1;
        } else {
            int[] pos = {line + 1, 0};
            if (resolutionContext.functionImplementation.find(new EndMatcher(), pos) == null) {
                throw new RuntimeException("END not found for multiline ON.");
            }
            lineBeyondEnd = pos[0] + 1;
        }
    }

    @Override
    public Object eval(EvaluationContext evaluationContext) {
        context = new EvaluationContext(evaluationContext);
        context.currentIndex++;
        children[0].accept(new ListenerAttachmentVisitor(evaluationContext));

        // TODO: jump to END instead if available
        evaluationContext.currentLine++;
        return null;
    }

    @Override
    public void propertyChanged(Property<?> property) {
        if (evalChildToBoolean(context, 0)) {
            new Thread(() -> context.function.callImpl(new EvaluationContext(context))).start();
        }
    }


    class ListenerAttachmentVisitor extends Visitor {

        EvaluationContext evaluationContext;

        public ListenerAttachmentVisitor(EvaluationContext evaluationContext) {
            this.evaluationContext = evaluationContext;
        }


        @Override
        public void visitIdentifier(Identifier identifier) {
            identifier.eval(evaluationContext);

        }


        @Override
        public void visitPath(Path path) {
            Property property = path.evalProperty(evaluationContext);
            property.addListener(OnStatement.this);
        }
    }


    static class EndMatcher implements FunctionImplementation.StatementMatcher {
        int skip;

        @Override
        public boolean statementMatches(CodeLine line, int index, Node statement) {
            if (statement instanceof OnStatement) {
                // Multiline?
                if (index == line.length() - 1) {
                    skip++;
                }
            } else if (statement instanceof EndStatement) {
                if (skip == 0) {
                    return true;
                }
                skip--;
            }
            return false;
        }
    }
}
