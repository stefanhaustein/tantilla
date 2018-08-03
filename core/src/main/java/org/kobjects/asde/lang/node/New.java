package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.classifier.Classifier;

public class New extends Node {
    final String name;
    final Classifier classifier;

    public New(Program program, String name) {
        super();
        this.name = name;
        classifier = program.classifiers.get(name);
        if (classifier == null) throw new RuntimeException("Unrecognized class: " + name);
    }

    @Override
    public Object eval(Interpreter interpreter) {
        return classifier.createInstance();
    }

    @Override
    public Object returnType() {
        return classifier;
    }

    public String toString() {
        return "new " + name;
    }
}
