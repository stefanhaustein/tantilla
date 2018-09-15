package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.StackEntry;
import org.kobjects.asde.lang.Types;
import org.kobjects.asde.lang.parser.ResolutionContext;
import org.kobjects.asde.lang.symbol.ResolvedSymbol;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class ForStatement extends Node {
    private final String varName;
    ResolvedSymbol resolved;

    public ForStatement(String varName, Node... children) {
        super(children);
        this.varName = varName;
    }


    public void resolve(ResolutionContext resolutionContext) {
        super.resolve(resolutionContext);
        resolved = resolutionContext.resolve(varName);
        if (resolved == null) {
            throw new RuntimeException("Identifier not found: " + varName);
        }
    }


    @Override
    public Object eval(Interpreter interpreter) {
            double current = evalDouble(interpreter,0);
            resolved.set(interpreter, current);
            double end = evalDouble(interpreter, 1);
            double step = children.length > 2 ? evalDouble(interpreter, 2) : 1.0;
            if (Math.signum(step) == Math.signum(Double.compare(current, end))) {
                int nextPosition[] = new int[3];
                if (interpreter.program.find(Statement.Kind.NEXT, children[0].toString(), nextPosition) == null) {
                    throw new RuntimeException("FOR without NEXT");
                }
                interpreter.currentLine = nextPosition[0];
                interpreter.currentIndex = nextPosition[1];
                interpreter.nextSubIndex = nextPosition[2] + 1;
            } else {
                StackEntry entry = new StackEntry();
                entry.forVariable = (Identifier) children[0];
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
