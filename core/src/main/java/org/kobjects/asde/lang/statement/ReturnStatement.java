package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.StackEntry;
import org.kobjects.asde.lang.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class ReturnStatement extends Node {

    public ReturnStatement(Node... children) {
        super(children);
    }

    @Override
    public Object eval(Interpreter interpreter) {
        if (children.length > 0) {
            interpreter.returnValue = children[0].eval(interpreter);
            interpreter.currentLine = Integer.MAX_VALUE;
            interpreter.currentIndex = 0;
        } else {
            while (true) {
                if (interpreter.stack.isEmpty()) {
                    throw new RuntimeException("RETURN without GOSUB.");
                }
                StackEntry entry = interpreter.stack.remove(interpreter.stack.size() - 1);
                if (entry.forVariable == null) {
                    interpreter.currentLine = entry.lineNumber;
                    interpreter.currentIndex = entry.statementIndex + 1;
                    break;
                }
            }
        }
        return null;
    }

    @Override
    public Type returnType() {
        return Types.VOID;
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
        appendLinked(asb, "RETURN", errors);
        if (children.length > 0) {
            asb.append(' ');
            children[0].toString(asb, errors);
        }
    }
}
