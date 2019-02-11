package org.kobjects.asde.lang.statement;

import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.asde.lang.node.Identifier;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.node.Path;
import org.kobjects.asde.lang.node.Visitor;
import org.kobjects.typesystem.Property;
import org.kobjects.typesystem.PropertyChangeListener;

public class OnStatement extends Statement implements PropertyChangeListener {

    EvaluationContext context;

    public OnStatement(Node condition) {
        super(condition);
    }

    @Override
    protected void onResolve(FunctionValidationContext resolutionContext, int line, int index) {

    }

    @Override
    public Object eval(EvaluationContext evaluationContext) {
        context = new EvaluationContext(evaluationContext);
        children[0].accept(new ListenerAttachmentVisitor(evaluationContext));

        // jump to END

        return null;
    }

    @Override
    public void propertyChanged(Property<?> property) {
        // TODO: Evaluate expr in context
        System.out.println("Property changed: " + property);
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
}
