package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Types;
import org.kobjects.asde.lang.symbol.GlobalSymbol;
import org.kobjects.typesystem.Classifier;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class Declaration extends Node {
    String typeName;
    String variableName;

    public Declaration(String typeName, String varName) {
        this.typeName = typeName;
        this.variableName = varName;
    }

    @Override
    public Object eval(Interpreter interpreter) {
        GlobalSymbol symbol = interpreter.program.getSymbol(typeName);
        Type type = (Type) symbol.value;
        Object value;
        if (type instanceof Classifier) {
            value = ((Classifier) type).createInstance();
        } else if (type == Types.NUMBER) {
            value = 0.0;
        } else if (type == Types.STRING) {
            value = "";
        } else {
            throw new RuntimeException("Unrecognized type: " + typeName);
        }
        interpreter.program.setValue(interpreter.getSymbolScope(), variableName, value);
        return null;
    }

    @Override
    public Type returnType() {
        return null;
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
        appendLinked(asb, typeName + " " + variableName, errors);
    }
}
