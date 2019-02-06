package org.kobjects.asde.lang;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.statement.ElseStatement;
import org.kobjects.asde.lang.statement.EndIfStatement;
import org.kobjects.asde.lang.statement.ForStatement;
import org.kobjects.asde.lang.statement.IfStatement;
import org.kobjects.asde.lang.statement.NextStatement;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.typesystem.FunctionType;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CallableUnit implements Function {
    public final Program program;
    FunctionType type;
    public String[] parameterNames;
    private TreeMap<Integer, CodeLine> code = new TreeMap<>();
    private int localVariableCount;

    public CallableUnit(Program program, FunctionType type, String... parameterNames) {
        this.program = program;
        this.type = type;
        this.parameterNames = parameterNames;
    }

    public FunctionValidationContext validate(ProgramValidationContext programValidationContext) {
        FunctionValidationContext resolutionContext = new FunctionValidationContext(programValidationContext,
                this == program.main && program.legacyMode ? FunctionValidationContext.ResolutionMode.LEGACY : FunctionValidationContext.ResolutionMode.STRICT,
                this, parameterNames);

        int indent = 0;
        for (Map.Entry<Integer,CodeLine> entry : code.entrySet()) {
            int addLater = 0;
            CodeLine line = entry.getValue();
            for (int i = 0; i < line.statements.size(); i++) {
                Node statement = line.statements.get(i);
                if (statement instanceof ElseStatement && ((ElseStatement) statement).multiline) {
                    addLater++;
                    indent--;
                } else if (statement instanceof ForStatement
                        || (statement instanceof IfStatement
                        && ((IfStatement) statement).multiline
                        && !((IfStatement) statement).elseIf)) {
                    addLater++;
                } else if (statement instanceof NextStatement || statement instanceof EndIfStatement) {
                    if (addLater > 0) {
                        addLater--;
                    } else {
                        indent--;
                    }
                }
                statement.resolve(resolutionContext, entry.getKey(), i);
            }
            line.indent = indent;
            indent += addLater;
        }
        localVariableCount = resolutionContext.getLocalVariableCount();
        return resolutionContext;
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
        Interpreter sub = new Interpreter(interpreter.control, this, interpreter.localStack);
        try {
            // This is called from inside ast evaluation, we can't push interpreter state
            // and keep
            sub.runCallableUnit();
            return sub.returnValue;
        } catch (Exception e) {
            throw new WrappedExecutionException(this, sub.currentLine, e);
        } finally {
            interpreter.localStack.release(oldFrameStart, paramCount);
        }
    }

    public void toString(AnnotatedStringBuilder sb, String name, Map<Node, Exception> errors) {
        boolean sub = type.getReturnType() == Types.VOID;
        String kind = sub ? "SUB" : "FUNCTION";
        if (name != null) {
            sb.append(kind);
            sb.append(' ');
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
            sb.append(")");
            if (!sub) {
                sb.append(" -> ");
                sb.append(type.getReturnType().toString());
            }
            sb.append('\n');
        }

        for (Map.Entry<Integer, CodeLine> entry : code.entrySet()) {
            sb.append(String.valueOf(entry.getKey()));
            sb.append(' ');
            entry.getValue().toString(sb, errors);
            sb.append('\n');
        }

        if (name != null) {
            sb.append("END ").append(kind).append("\n\n");
        }

    }

    public synchronized void setLine(int number, CodeLine line) {
        if (line == null) {
            code.remove(number);
        } else {
            code.put(number, line);
        }
        program.notifyProgramChanged();
    }

    public Map.Entry<Integer,CodeLine> ceilingEntry(int i) {
        return code.ceilingEntry(i);
    }

    public synchronized Iterable<Map.Entry<Integer, CodeLine>> entrySet() {
        return new LinkedHashSet<Map.Entry<Integer, CodeLine>>(code.entrySet());
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

    public void setType(FunctionType functionType) {
        this.type = functionType;
    }

    public Iterable<Node> statements(int fromLine, int fromIndex, int toLine, int toIndex) {
        return () -> new StatementIterator(fromLine, fromIndex, toLine, toIndex);
    }

    public Iterable<Node> descendingStatements(int fromLine, int fromIndex, int toLine, int toIndex) {
        return () -> new DescendingStatementIterator(fromLine, fromIndex, toLine, toIndex);
    }

    public interface StatementMatcher {
        boolean statementMatches(Node statement);
    }

    class StatementIterator implements Iterator<Node> {
        final Iterator<Map.Entry<Integer, CodeLine>> lineIterator;

        private Node next;
        private Map.Entry<Integer, CodeLine> currentLine;
        int index;
        int toLine;
        int toIndex;

        StatementIterator(int fromLine, int fromIndex, int toLine, int toIndex) {
            lineIterator = code.subMap(fromLine, true, toLine, true).entrySet().iterator();
            currentLine = lineIterator.hasNext() ? lineIterator.next() : null;
            index = fromIndex;
            this.toLine = toLine;
            this.toIndex = toIndex;
        }

        public boolean hasNext() {
            if (next != null) {
                return true;
            }
            if (currentLine == null) {
                return false;
            }
            while (index >= currentLine.getValue().statements.size()) {
                if (!lineIterator.hasNext()) {
                    return false;
                }
                currentLine = lineIterator.next();
                index = 0;
            }
            if (currentLine.getKey() == toLine && index >= toIndex) {
                return false;
            }
            next = currentLine.getValue().statements.get(index++);
            return true;
        }

        public Node next() {
            if (next == null && !hasNext()) {
                throw new IndexOutOfBoundsException();
            }
            Node result = next;
            next = null;
            return result;
        }
    }

    class DescendingStatementIterator implements Iterator<Node> {
        final Iterator<Map.Entry<Integer, CodeLine>> lineIterator;

        private Node next;
        private Map.Entry<Integer, CodeLine> currentLine;
        int index;
        int toLine;
        int toIndex;

        DescendingStatementIterator(int fromLine, int fromIndex, int toLine, int toIndex) {
            lineIterator = code.subMap(toLine, true, fromLine, true).descendingMap().entrySet().iterator();
            currentLine = lineIterator.hasNext() ? lineIterator.next() : null;
            index = fromIndex;
            this.toLine = toLine;
            this.toIndex = toIndex;
        }

        public boolean hasNext() {
            if (next != null) {
                return true;
            }
            if (currentLine == null) {
                return false;
            }
            while (index < 0) {
                if (!lineIterator.hasNext()) {
                    return false;
                }
                currentLine = lineIterator.next();
                index = currentLine.getValue().statements.size() - 1;
            }
            if (currentLine.getKey() == toLine && index <= toIndex) {
                return false;
            }
            next = currentLine.getValue().statements.get(index--);
            return true;
        }

        public Node next() {
            if (next == null && !hasNext()) {
                throw new IndexOutOfBoundsException();
            }
            Node result = next;
            next = null;
            return result;
        }
    }

}
