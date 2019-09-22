package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.type.Array;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.typesystem.MetaType;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class DimStatement extends Node {
    public final String varName;
    public Type elementType;

    public DimStatement(Type elementType, String varName, Node... children) {
        super(children);
        this.elementType = elementType;
        this.varName = varName;
    }

    @Override
    protected void onResolve(FunctionValidationContext resolutionContext, int line, int index) {
        // Don't need to do anything -- child resolution is handled in resolve()
    }

    @Override
    public Object eval(EvaluationContext evaluationContext) {
        int[] dims = new int[children.length];
        for (int i = 0; i < children.length; i++) {
             // TODO: evalChildToInt
             dims[i] = evalChildToInt(evaluationContext, i);
        }
        evaluationContext.control.program.setValue(evaluationContext.getSymbolScope(), varName, new Array(elementType, dims));
        return null;
    }

    @Override
    public Type returnType() {
        return Types.VOID;
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
        appendLinked(asb, "DIM " + varName + "(", errors);
        if (children.length > 0) {
            children[0].toString(asb, errors);
            for (int i = 1; i < children.length; i++) {
                asb.append(", ");
                children[i].toString(asb, errors);
            }
        }
        asb.append(") AS ");
        asb.append(elementType.toString());
    }
}
