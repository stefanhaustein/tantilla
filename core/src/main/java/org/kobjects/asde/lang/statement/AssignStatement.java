package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.node.AssignableNode;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class AssignStatement extends Node {
    public AssignStatement(Node target, Node value) throws Exception {
        super(target, value);
        if (!(target instanceof AssignableNode)) {
            throw new Exception("Assignment target is not assignable.");
        }
    }

    @Override
    protected void onResolve(FunctionValidationContext resolutionContext, int line, int index) {
        if (resolutionContext.mode == FunctionValidationContext.ResolutionMode.LEGACY) {
            return;
        }
        if (!Types.match(children[0].returnType(), children[1].returnType())) {
            throw new RuntimeException("Cannot assign " + children[1].returnType() + " to " + children[0].returnType());
        }
        if (((AssignableNode) children[0]).isConstant()) {
            throw new RuntimeException("Cannot assign to a constant.");
        }
    }

    @Override
    public Object eval(EvaluationContext evaluationContext) {
        ((AssignableNode) children[0]).set(evaluationContext, children[1].eval(evaluationContext));
        return null;
    }

    @Override
    public Type returnType() {
        return Types.VOID;
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
        children[0].toString(asb, errors);
        appendLinked(asb, " = ", errors);
        children[1].toString(asb, errors);
    }
}
