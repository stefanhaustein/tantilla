package org.kobjects.asde.lang;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.node.Statement;
import org.kobjects.asde.lang.parser.ResolutionContext;
import org.kobjects.typesystem.FunctionType;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class CallableUnit implements Function {
    final Program program;
    FunctionType type;
    String[] parameterNames;
    private TreeMap<Integer, CodeLine> code = new TreeMap<>();
    public HashMap<Node, Exception> errors = new HashMap<>();
    private int localVariableCount;

    public CallableUnit(Program program, FunctionType type, String... parameterNames) {
        this.program = program;
        this.type = type;
        this.parameterNames = parameterNames;
    }

    public void resolve() {
        ResolutionContext resolutionContext = new ResolutionContext(program,
                this == program.main ? ResolutionContext.ResolutionMode.MAIN : ResolutionContext.ResolutionMode.FUNCTION,
                type, parameterNames);

        int indent = 0;
        for (CodeLine line : code.values()) {
            line.indent = indent;
            for (Node statement : line.statements) {
                if (statement instanceof Statement) {
                    switch (((Statement) statement).kind) {
                        case FOR:
                            indent++;
                            break;
                        case NEXT:
                            line.indent--;
                            indent--;
                            break;
                    }
                }
                try {
                    statement.resolve(resolutionContext);
                } catch (Exception e) {
                    resolutionContext.addError(statement, e);
                }
            }
        }

        errors = resolutionContext.errors;
        localVariableCount = resolutionContext.getLocalVariableCount();
        for (Exception exception : errors.values()) {
            exception.printStackTrace();
        }
    }

    @Override
    public FunctionType getType() {
        return type;
    }

    @Override
    public int getLocalVariableCount() {
        return localVariableCount;
    }

    @Override
    public Object eval(Interpreter interpreter, Object[] parameterValues) {
        return interpreter.call(this, parameterValues);
    }

    public void toString(AnnotatedStringBuilder sb, String name) {
        if (name != null) {
            sb.append("FUNCTION ");
            sb.append(name);
            sb.append("(");
            for (int i = 0; i < parameterNames.length; i++) {
                if (i != 0) {
                    sb.append(", ");
                }
                sb.append(type.getParameterType(i).toString());
                sb.append(' ');
                sb.append(parameterNames[i]);
            }
            sb.append(") -> ");
            sb.append(type.getReturnType().toString());
            sb.append("\n");
        }

        for (Map.Entry<Integer, CodeLine> entry : code.entrySet()) {
            sb.append(String.valueOf(entry.getKey()));
            sb.append(' ');
            entry.getValue().toString(sb, errors);
            sb.append('\n');
        }

        if (name != null) {
            sb.append("END FUNCTION\n\n");
        }

    }

    public void setLine(int number, CodeLine line) {
        code.put(number, line);
        resolve();
    }

    public Map.Entry<Integer,CodeLine> ceilingEntry(int i) {
        return code.ceilingEntry(i);
    }

    public Iterable<Map.Entry<Integer, CodeLine>> entrySet() {
        return code.entrySet();
    }

    public void clear() {
        code = new TreeMap<>();
    }

    public int getLineCount() {
        return code.size();
    }
}
