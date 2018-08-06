package org.kobjects.asde.lang;

import org.kobjects.asde.lang.node.Statement;
import org.kobjects.typesystem.FunctionType;

import java.util.List;
import java.util.TreeMap;

public class CallableUnit {
    final Program program;
    FunctionType type;
    public TreeMap<Integer, List<Statement>> code = new TreeMap<>();

    public CallableUnit(Program program) {
        this.program = program;
    }

    public void resolve() {
        ResolutionContext resolutionContext = new ResolutionContext(program);

        for (List<Statement> statements : code.values()) {
            for (Statement statement : statements) {
                try {
                    statement.resolve(resolutionContext);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
