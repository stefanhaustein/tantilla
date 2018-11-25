package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.StackEntry;
import org.kobjects.asde.lang.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.parser.ResolutionContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class FunctionReturnStatement extends Node {

    public FunctionReturnStatement(Node... children) {
        super(children);
    }

    @Override
    protected void onResolve(ResolutionContext resolutionContext) {
        if (resolutionContext.functionType.getReturnType() == Types.VOID) {
            if (children.length != 0) {
                throw new RuntimeException("Unexpected return value for subroutine.");
            }
        } else {
            if (children.length != 1) {
                throw new RuntimeException("Return value expected for function.");
            }
            if (children[0].returnType().equals(resolutionContext.functionType)) {
                throw new RuntimeException("Expected return type: " + resolutionContext.functionType + "; actual: " + children[0].returnType());
            }
        }
    }

    @Override
    public Object eval(Interpreter interpreter) {
        if (children.length > 0) {
            interpreter.returnValue = children[0].eval(interpreter);
        }
        interpreter.currentLine = Integer.MAX_VALUE;
        interpreter.currentIndex = 0;
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
