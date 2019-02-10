package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.asde.lang.ResolvedSymbol;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class LetStatement extends Node {
    public final String varName;
    ResolvedSymbol resolved;
    boolean constant;

    public LetStatement(String varName, Node init, boolean constant) {
        super(init);
        this.constant = constant;
        this.varName = varName;
    }

    public void onResolve(FunctionValidationContext resolutionContext, int line, int index) {
        resolved = resolutionContext.declare(varName, children[0].returnType(), constant);
    }

    @Override
    public Object eval(EvaluationContext evaluationContext) {
        Object value = children[0].eval(evaluationContext);
        resolved.set(evaluationContext, value);
        return null;
    }

    @Override
    public Type returnType() {
        return Types.VOID;
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
        appendLinked(asb, (constant ? "CONST " : "LET ") + varName + " = ", errors);
        children[0].toString(asb, errors);
    }

    public boolean isConstant() {
        return constant;
    }
}
