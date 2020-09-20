package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;
import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.list.ListImpl;
import org.kobjects.asde.lang.list.ListType;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Arrays;
import java.util.Map;

public class ArrayLiteral extends ExpressionNode {
    ListType resolvedType;

    public ArrayLiteral(ExpressionNode... children) {
        super(children);
    }

    @Override
    protected Type resolveWasmImpl(WasmExpressionBuilder wasm, ValidationContext resolutionContext, int line) {
        final int count = children.length;
        if (count == 0) {
            resolvedType = new ListType(null, 0);
            wasm.callWithContext(context -> context.push(new ListImpl(resolvedType.elementType)));
        } else {
            Type elementType = children[0].resolveWasm(wasm, resolutionContext, line);
            for (int i = 1; i < count; i++) {
                Type typeI = children[i].resolveWasm(wasm, resolutionContext, line);
                if (!elementType.equals(typeI)) {
                    throw new RuntimeException("Type mismatch. Expected: " + elementType + " but was " + children[i].returnType());
                }
            }
            resolvedType = new ListType(elementType);
            wasm.callWithContext(context -> {
                Object[] array = new Object[count];
                for (int i = count-1; i >= 0; i--) {
                    array[i] = context.dataStack.popObject();
                }
                System.err.println("Array constant evaluated to: " + Arrays.toString(array));
                context.push(new ListImpl(resolvedType.elementType, array));
            });
        }
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
