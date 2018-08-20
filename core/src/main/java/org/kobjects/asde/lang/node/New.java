package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.symbol.GlobalSymbol;
import org.kobjects.typesystem.Classifier;

import java.util.Map;

public class New extends Node {
    final String name;
    Classifier classifier;

    public New(Program program, String name) {
        super();
        this.name = name;
        GlobalSymbol symbol = program.getSymbol(name);
        if (symbol == null) {
            throw new RuntimeException("'" + name + "' is not defined");
        }
        Object value = symbol.value;
        if (!(value instanceof Classifier)) {
            throw new RuntimeException("'" + name + "' is not a classifier");
        }
        classifier = (Classifier) value;
    }

    @Override
    public Object eval(Interpreter interpreter) {
        return classifier.createInstance();
    }

    @Override
    public Classifier returnType() {
        return classifier;
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node,Exception> errors) {
        appendLinked(asb, "new " + name, errors);
    }
}
