package org.kobjects.asde.lang.node;

import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.list.ListImpl;
import org.kobjects.asde.lang.list.ListType;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;

public class ArrayLiteral extends Node {
    ListType resolvedType;

    public ArrayLiteral(Node... children) {
        super(children);
    }

    @Override
    protected void onResolve(ValidationContext resolutionContext, int line) {
        if (children.length == 0) {
            resolvedType = new ListType(null, 0);
        } else {
            Type elementType = children[0].returnType();
            for (int i = 0; i < children.length; i++) {
                if (!elementType.equals(children[i].returnType())) {
                    throw new RuntimeException("Type mismatch. Expected: " + elementType + " but was " + children[i].returnType());
                }
            }
            resolvedType = new ListType(elementType);
        }
    }

    @Override
    public Object eval(EvaluationContext evaluationContext) {
        Object[] values = new Object[children.length];
        for (int i = 0; i < children.length; i++) {
            values[i] = children[i].eval(evaluationContext);
        }
        return new ListImpl(resolvedType.elementType, values);
    }

    @Override
    public Type returnType() {
        return resolvedType;
    }


    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
        appendLinked(asb, "{", errors);
        for (int i = 0; i < children.length; i++) {
            if (i > 0) {
                asb.append(", ");
            }
            children[i].toString(asb, errors, preferAscii);
        }
        appendLinked(asb, "}", errors);
    }
}
