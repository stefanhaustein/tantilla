package org.kobjects.asde.lang;

import org.kobjects.asde.lang.symbol.GlobalSymbol;

import java.util.LinkedHashSet;

public class ProgramValidationContext {
    static int stampFactory;

    public final int stamp = ++stampFactory;
    LinkedHashSet<String> dependencyChain = new LinkedHashSet<>();
    Program program;

    ProgramValidationContext(Program program) {
        this.program = program;
    }

    void resetChain(String name) {
        dependencyChain.clear();
        dependencyChain.add(name);
    }

    GlobalSymbol resolve(String name) {
        if (dependencyChain.contains(name)) {
            throw new RuntimeException("Circular dependency: " + dependencyChain + " -> " + name);
        }
        GlobalSymbol symbol = program.getSymbol(name);
        if (symbol == null) {
            return null;
        }
        if (symbol.validationStamp != stamp) {
            dependencyChain.add(name);
            symbol.validate(this);
            dependencyChain.remove(name);
        }
        return symbol;
    }


}
