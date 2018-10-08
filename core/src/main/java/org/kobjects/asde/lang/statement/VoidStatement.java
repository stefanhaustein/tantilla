package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.CallableUnit;
import org.kobjects.asde.lang.Function;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class VoidStatement extends Node {
    public VoidStatement(Node expression) {
        super(expression);
    }

    @Override
    public Object eval(Interpreter interpreter) {
        Object result = children[0].eval(interpreter);
        if (result instanceof CallableUnit) {
            ((CallableUnit) result).eval(interpreter, new Object[((CallableUnit) result).getLocalVariableCount()]);
        }
        return result;
    }

    @Override
    public Type returnType() {
        return null;
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
        children[0].toString(asb, errors);
    }
}
