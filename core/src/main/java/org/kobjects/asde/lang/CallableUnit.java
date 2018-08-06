package org.kobjects.asde.lang;

import org.kobjects.asde.lang.node.Statement;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Parameter;
import org.kobjects.typesystem.Type;

import java.util.List;
import java.util.TreeMap;

public class CallableUnit implements Function {
    final Program program;
    FunctionType type;
    public TreeMap<Integer, List<Statement>> code = new TreeMap<>();

    public CallableUnit(Program program, Type returnType, Parameter... parameters) {
        this.program = program;
        this.type = new FunctionType(returnType, parameters);
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

    @Override
    public FunctionType getType() {
        return type;
    }

    @Override
    public Object eval(Interpreter interpreter, Object[] parameterValues) {
        Symbol[] saved = new Symbol[type.getParameterCount()];
        for (int i = 0; i < type.getParameterCount(); i++) {
            String param = type.getParameter(i).name;
            saved[i] = program.getSymbol(param);
            program.setSymbol(param, new Symbol(interpreter.getSymbolScope(), parameterValues[i]));
        }
        try {
            return interpreter.call(this);
        } finally {
           for (int i = 0; i < type.getParameterCount(); i++) {
              program.setSymbol(type.getParameter(i).name, saved[i]);
          }
       }

    }
}
