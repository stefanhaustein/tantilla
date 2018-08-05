package org.kobjects.asde.lang;


public class ResolutionContext {
    Program program;

    ResolutionContext(Program program) {
        this.program = program;
    }

    public Symbol getSymbol(String name) {
        return program.getSymbol(name);
    }
}
