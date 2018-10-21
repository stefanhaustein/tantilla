package org.kobjects.asde.lang;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.statement.ForStatement;
import org.kobjects.asde.lang.statement.NextStatement;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.parser.ResolutionContext;
import org.kobjects.typesystem.FunctionType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CallableUnit implements Function {
    public final Program program;
    FunctionType type;
    public String[] parameterNames;
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
            int addLater = 0;
            for (Node statement : line.statements) {
                if (statement instanceof ForStatement) {
                    addLater++;
                } else if (statement instanceof NextStatement) {
                    if (addLater > 0) {
                        addLater--;
                    } else {
                        indent--;
                    }
                }
                try {
                    statement.resolve(resolutionContext);
                } catch (Exception e) {
                    resolutionContext.addError(statement, e);
                }
            }
            line.indent = indent;
            indent += addLater;
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


    /**
     * Calls this method with a new interpreter.
     */
    @Override
    public Object call(Interpreter interpreter, int paramCount) {
        int oldFrameStart = interpreter.localStack.frame(paramCount, localVariableCount);
        try {
            // This is called from inside ast evaluation, we can't push interpreter state
            // and keep
            Interpreter sub = new Interpreter(interpreter.control, this, interpreter.localStack);
            sub.runCallableUnit();
            return sub.returnValue;
        } finally {
            interpreter.localStack.release(oldFrameStart, paramCount);
        }
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


    public Node find(StatementMatcher matcher, int[] position) {
        Map.Entry<Integer, CodeLine> entry;
        while (null != (entry = ceilingEntry(position[0]))) {
            position[0] = entry.getKey();
            List<Node> list = entry.getValue().statements;
            while (position[1] < list.size()) {
                Node statement = list.get(position[1]);
                if (matcher.statementMatches(statement)) {
                        return statement;
                }
                position[1]++;
            }
            position[0]++;
            position[1] = 0;
        }
        return null;
    }

    public interface StatementMatcher {
        boolean statementMatches(Node statement);
    }

}
