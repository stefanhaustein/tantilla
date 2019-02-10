package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.JumpStackEntry;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.asde.lang.ResolvedSymbol;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class ForStatement extends Node {
    private final String varName;
    ResolvedSymbol resolved;

    public ForStatement(String varName, Node... children) {
        super(children);
        this.varName = varName;
    }

    public void onResolve(FunctionValidationContext resolutionContext, int line, int index) {
        resolutionContext.startBlock(FunctionValidationContext.BlockType.FOR);
        // TODO: Check types?
        resolved = resolutionContext.declare(varName, Types.NUMBER, false);
    }

    @Override
    public Object eval(Interpreter interpreter) {
            double current = evalChildToDouble(interpreter,0);
            resolved.set(interpreter, current);
            double end = evalChildToDouble(interpreter, 1);
            double step = children.length > 2 ? evalChildToDouble(interpreter, 2) : 1.0;
            if (Math.signum(step) == Math.signum(Double.compare(current, end))) {
                int nextPosition[] = new int[3];
                if (interpreter.functionImplementation.find((Node statement) -> (statement instanceof NextStatement
                        && (((NextStatement) statement).varName == null || ((NextStatement) statement).varName.equals(varName))), nextPosition) == null) {
                    throw new RuntimeException("FOR without NEXT");
                }
                interpreter.currentLine = nextPosition[0];
                interpreter.currentIndex = nextPosition[1];
                interpreter.nextSubIndex = nextPosition[2] + 1;
            } else {
                JumpStackEntry entry = new JumpStackEntry();
                entry.forVariable = resolved;
                entry.forVariableName = varName;
                entry.end = end;
                entry.step = step;
                entry.lineNumber = interpreter.currentLine;
                entry.statementIndex = interpreter.currentIndex;
                interpreter.stack.add(entry);
            }
            return null;
    }

    @Override
    public Type returnType() {
        return Types.VOID;
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
        appendLinked(asb, "FOR " + varName + " = ", errors);
        children[0].toString(asb, errors);
        asb.append(" TO ");
        children[1].toString(asb, errors);
        if (children.length > 2) {
            asb.append(" STEP ");
            children[2].toString(asb, errors);
        }
    }
}
