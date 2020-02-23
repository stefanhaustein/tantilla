package org.kobjects.asde.lang.program;

import org.kobjects.asde.lang.symbol.StaticSymbol;

import java.util.HashSet;
import java.util.LinkedHashSet;

public class ProgramValidationContext {
    final LinkedHashSet<String> dependencyChain = new LinkedHashSet<>();
    public final Program program;
    final HashSet<StaticSymbol> validated = new HashSet<>();

    public ProgramValidationContext(Program program) {
        this.program = program;
    }

    void startChain(String name) {
        dependencyChain.clear();
        dependencyChain.add(name);
    }


}
