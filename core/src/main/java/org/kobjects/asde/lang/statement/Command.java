package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.AsdeShell;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.typesystem.Type;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

public class Command extends Node {

    public enum Kind {
        CLEAR, CONTINUE, DUMP,
        LIST, LOAD,
        RUN,SAVE, TRON, TROFF
    }

    private final Kind kind;

    public Command(Kind kind, Node... children) {
        super(children);
        this.kind = kind;
    }

    @Override
    public Object eval(Interpreter interpreter) {
        Program program = interpreter.program;
        switch (kind) {
            case CONTINUE:
                if (program.stopped == null) {
                    throw new RuntimeException("Not stopped.");
                }
                interpreter.currentLine = program.stopped[0];
                interpreter.currentIndex = program.stopped[1] + 1;
                break;

            case CLEAR:
                program.clear(interpreter);
                break;
            case DUMP:
                if (program.lastException != null) {
                    program.lastException.printStackTrace();
                    program.lastException = null;
                } else {
                    program.println("\n" + program.getSymbolMap());

        /*  for (int i = 0; i < program.arrays.length; i++) {
            if (!program.arrays[i].isEmpty()) {
              program.println((i + 1) + ": " + program.arrays[i]);
            }
          } */
                }
                break;

            case LIST: {
                program.print(program.toString());
                break;
            }

            case LOAD:
                load(interpreter);
                break;


            case RUN:
                program.clear(interpreter);

                interpreter.currentLine = children.length == 0 ? 0 : (int) evalDouble(interpreter,0);
                interpreter.currentIndex = 0;
                break;

            case SAVE:
                program.save(children.length == 0 ? null : evalString(interpreter,0));
                break;

            case TRON:
                program.trace = true;
                break;
            case TROFF:
                program.trace = false;
                break;

        }
        return null;
    }

    void load(Interpreter interpreter) {
        String line = null;
        try {
            URLConnection connection = new URL(evalString(interpreter, 0)).openConnection();
            connection.setDoInput(true);
            InputStream is = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            while (null != (line = reader.readLine())) {
                try {
                    AsdeShell.processInputLine(interpreter, line);
                } catch (Exception e) {
                    interpreter.program.println(line);
                    interpreter.program.println(e.getMessage());
                }
            }
            reader.close();
            is.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Type returnType() {
        return Types.VOID;
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
        appendLinked(asb, kind.name(), errors);
        if (children.length > 0) {
            appendLinked(asb, " ", errors);
            children[0].toString(asb, errors);
            for (int i = 1; i < children.length; i++) {
                asb.append(", ");
                children[i].toString(asb, errors);
            }
        }
    }

}
