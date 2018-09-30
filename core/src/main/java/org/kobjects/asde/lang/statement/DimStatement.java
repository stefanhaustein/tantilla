package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Array;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Types;
import org.kobjects.asde.lang.node.Apply;
import org.kobjects.asde.lang.node.Identifier;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class DimStatement extends Node {
    public DimStatement(Node... children) {
        super(children);
    }

    @Override
    public Object eval(Interpreter interpreter) {
        for (Node expr : children) {
            if (!(expr instanceof Apply)) {
                throw new RuntimeException("DIM Syntax error");
            }
            if (!(expr.children[0] instanceof Identifier)) {
                throw new RuntimeException("DIM identifier expected");
            }
            String name = ((Identifier) expr.children[0]).name;
            int[] dims = new int[expr.children.length - 1];
            for (int i = 0; i < dims.length; i++) {
                // TODO: evalInt
                dims[i] = ((Number) expr.children[i + 1].eval(interpreter)).intValue();
            }
            interpreter.program.setValue(interpreter.getSymbolScope(), name, new Array(name.endsWith("$") ? Types.STRING : Types.NUMBER, dims));
        }
        return null;
    }

    @Override
    public Type returnType() {
        return Types.VOID;
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
        appendLinked(asb, "DIM ", errors);
        if (children.length > 0) {
            children[0].toString(asb, errors);
            for (int i = 1; i < children.length; i++) {
                asb.append(", ");
                children[i].toString(asb, errors);
            }
        }
    }
}
