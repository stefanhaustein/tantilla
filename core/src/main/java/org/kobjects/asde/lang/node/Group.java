package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;
import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;

public class Group extends ExpressionNode {
    public Group(ExpressionNode child) {
        super(child);
    }

    @Override
    protected Type resolveWasmImpl(WasmExpressionBuilder wasm, ValidationContext resolutionContext, int line) {
        return resolvedType = children[0].resolveWasm(wasm, resolutionContext, line);
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
        appendLinked(asb, "(", errors);

        children[0].toString(asb, errors, preferAscii);
        appendLinked(asb, ")", errors);
    }
}
