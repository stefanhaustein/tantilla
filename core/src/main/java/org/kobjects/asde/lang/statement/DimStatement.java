package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Array;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class DimStatement extends Node {
    public final String varName;

    public DimStatement(String varName, Node... children) {
        super(children);
        this.varName = varName;
    }

    @Override
    public Object eval(Interpreter interpreter) {
        int[] dims = new int[children.length];
        for (int i = 0; i < children.length; i++) {
             // TODO: evalInt
             dims[i] = evalInt(interpreter, i);
        }
        interpreter.program.setValue(interpreter.getSymbolScope(), varName, new Array(varName.endsWith("$") ? Types.STRING : Types.NUMBER, dims));
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