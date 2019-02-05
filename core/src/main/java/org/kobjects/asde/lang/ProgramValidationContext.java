package org.kobjects.asde.lang;

import org.kobjects.asde.lang.symbol.GlobalSymbol;

import java.util.HashSet;
import java.util.LinkedHashSet;

public class ProgramValidationContext {

    LinkedHashSet<String> dependencyChain = new LinkedHashSet<>();
    Program program;
    public HashSet<GlobalSymbol> validated = new HashSet<>();

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
        if (!validated.contains(symbol)) {
            dependencyChain.add(name);
            symbol.validate(this);
            dependencyChain.remove(name);
        }
        return symbol;
    }


}
