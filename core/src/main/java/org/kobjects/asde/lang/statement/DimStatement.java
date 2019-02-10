package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.type.Array;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class DimStatement extends Node {
    public final String varName;

    public DimStatement(String varName, Node... children) {
        super(children);
        this.varName = varName;
    }

    @Override
    protected void onResolve(FunctionValidationContext resolutionContext, int line, int index) {
        // TODO: Implement
        System.err.println("TODO: DimStatement.onResolve");
    }

    @Override
    public Object eval(EvaluationContext evaluationContext) {
        int[] dims = new int[children.length];
        for (int i = 0; i < children.length; i++) {
             // TODO: evalChildToInt
             dims[i] = evalChildToInt(evaluationContext, i);
        }
        evaluationContext.control.program.setValue(evaluationContext.getSymbolScope(), varName, new Array(varName.endsWith("$") ? Types.STRING : Types.NUMBER, dims));
        return null;
    }



    @Override
    public Type returnType() {
        return Types.VOID;
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
        appendLinked(asb, "DIM " + varName, errors);
        asb.append('(');
        if (children.length > 0) {
            children[0].toString(asb, errors);
            for (int i = 1; i < children.length; i++) {
                asb.append(", ");
                children[i].toString(asb, errors);
            }
        }
        asb.append(')');
    }
}
