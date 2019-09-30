package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.type.Array;
import org.kobjects.asde.lang.type.ArrayType;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class ArrayLiteral extends Node {
    ArrayType resolvedType;

    public ArrayLiteral(Node... children) {
        super(children);
    }

    @Override
    protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
        if (children.length == 0) {
            resolvedType = new ArrayType(null, 0);
        } else {
            Type elementType = children[0].returnType();
            for (int i = 0; i < children.length; i++) {
                if (!elementType.equals(children[i].returnType())) {
                    throw new RuntimeException("Type mismatch. Expected: " + elementType + " but was " + children[i].returnType());
                }
            }
            resolvedType = new ArrayType(elementType);
        }
    }

    @Override
    public Object eval(EvaluationContext evaluationContext) {
        Object[] values = new Object[children.length];
        for (int i = 0; i < children.length; i++) {
            values[i] = children[i].eval(evaluationContext);
        }
        return new Array(resolvedType.getReturnType(), values);
    }

    @Override
    public Type returnType() {
        return resolvedType;
    }


    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
        appendLinked(asb, "{", errors);
        for (int i = 0; i < children.length; i++) {
            if (i > 0) {
                asb.append(", ");
            }
            children[i].toString(asb, errors);
        }
        appendLinked(asb, "}", errors);
    }
}
