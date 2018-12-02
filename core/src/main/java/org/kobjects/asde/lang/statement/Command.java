package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.parser.ResolutionContext;
import org.kobjects.typesystem.Type;

import java.io.IOException;
import java.util.Map;

public class Command extends Node {

    public enum Kind {
        CLEAR, CONTINUE,
        DELETE, DUMP,
        EDIT,
        LIST, LOAD,
        RUN,SAVE, TRON, TROFF
    }

    private final Kind kind;

    public Command(Kind kind, Node... children) {
        super(children);
        this.kind = kind;
    }

    @Override
    protected void onResolve(ResolutionContext resolutionContext, int line, int index) {
        // Commands can't be in programs.
    }

    @Override
    public Object eval(Interpreter interpreter) {
        Program program = interpreter.control.program;
        switch (kind) {
            case CONTINUE:
                // This needs to go to the main control!!
                interpreter.control.resume();

            case CLEAR:
                program.clear(interpreter);
                break;

            case DELETE:
                program.console.delete(evalChildToInt(interpreter, 0));
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

            case EDIT:
                program.console.edit(evalChildToInt(interpreter, 0));
                break;

            case LIST: {
                program.print(program.toString());
                break;
            }

            case LOAD:
                try {
                    program.load(program.console.nameToReference(evalChildToString(interpreter, 0)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;


            case RUN:
                program.clear(interpreter);

                interpreter.currentLine = children.length == 0 ? 0 : (int) evalChildToDouble(interpreter,0);
                interpreter.currentIndex = 0;
                break;

            case SAVE:
                try {
                    program.save(children.length == 0 ? program.reference : program.console.nameToReference(evalChildToString(interpreter, 0)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;

            case TRON:
                interpreter.control.setTrace(true);
                break;
            case TROFF:
                interpreter.control.setTrace(false);
                break;

        }
        return null;
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
