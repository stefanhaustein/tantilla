package org.kobjects.asde.lang;

import java.util.HashSet;
import java.util.LinkedHashSet;

public class ProgramValidationContext {
    final LinkedHashSet<String> dependencyChain = new LinkedHashSet<>();
    final Program program;
    final HashSet<GlobalSymbol> validated = new HashSet<>();

    ProgramValidationContext(Program program) {
        this.program = program;
    }

    void startChain(String name) {
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
