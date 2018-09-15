package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Types;
import org.kobjects.asde.lang.parser.ResolutionContext;
import org.kobjects.asde.lang.symbol.ResolvedSymbol;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class LetStatement extends Node {
    public final String varName;
    ResolvedSymbol resolved;

    public LetStatement(String varName, Node init) {
        super(init);
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
        return null;
    }

    @Override
    public Type returnType() {
        return Types.VOID;
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
        appendLinked(asb, "LET " + varName + " = ", errors);
        children[0].toString(asb, errors);
    }
}
