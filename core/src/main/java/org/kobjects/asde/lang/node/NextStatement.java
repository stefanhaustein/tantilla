package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.StackEntry;
import org.kobjects.asde.lang.Types;
import org.kobjects.typesystem.Type;

public class NextStatement extends Node {

    public NextStatement(Node... children) {
        super(children);
    }

    @Override
    public Object eval(Interpreter interpreter) {
        for (int i = interpreter.nextSubIndex; i < Math.max(children.length, 1); i++) {
            String name = children.length == 0 ? null : children[i].toString();
            StackEntry entry;
            while (true) {
                if (interpreter.stack.isEmpty()
                        || interpreter.stack.get(interpreter.stack.size() - 1).forVariable == null) {
                    throw new RuntimeException("NEXT " + name + " without FOR.");
                }
                entry = interpreter.stack.remove(interpreter.stack.size() - 1);
                if (name == null || entry.forVariableName.equals(name)) {
                    break;
                }
            }
            double current = ((Double) entry.forVariable.get(interpreter)) + entry.step;
            entry.forVariable.set(interpreter, current);
            if (Math.signum(entry.step) != Math.signum(Double.compare(current, entry.end))) {
                interpreter.stack.add(entry);
                interpreter.currentLine = entry.lineNumber;
                interpreter.currentIndex = entry.statementIndex + 1;
                break;
            }
        }
        interpreter.nextSubIndex = 0;
        return null;
    }

    @Override
    public Type returnType() {
        return Types.VOID;
    }
}
