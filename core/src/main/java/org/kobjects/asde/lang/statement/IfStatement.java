package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class IfStatement extends Node {

    public IfStatement(Node condition) {
        super(condition);
    }

    @Override
    public Object eval(Interpreter interpreter) {
        if (evalDouble(interpreter, 0) == 0.0) {
            interpreter.currentLine++;
            interpreter.currentIndex = 0;
        }
        return null;
    }

    @Override
    public Type returnType() {
        return Types.VOID;
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
        appendLinked(asb, "IF ", errors);
        children[0].toString(asb, errors);
        asb.append(" THEN ");
    }
}
