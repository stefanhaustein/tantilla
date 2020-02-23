package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.node.Identifier;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.function.PropertyValidationContext;

import java.io.IOException;
import java.util.Map;

public class Command extends Statement {

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
  protected void onResolve(PropertyValidationContext resolutionContext, int line) {
    // Commands can't be in programs.
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    Program program = evaluationContext.control.program;
    switch (kind) {
      case CONTINUE:
        // This needs to go to the main control!!
        evaluationContext.control.resume();

      case CLEAR:
        program.clear(evaluationContext);
        break;

      case DELETE:
        program.console.delete(children[0].evalInt(evaluationContext));
        break;

      case DUMP:
        if (program.lastException != null) {
          program.lastException.printStackTrace();
          program.lastException = null;
        } else {
          program.println("\n" + program.getSymbols());

        /*  for (int i = 0; i < program.arrays.length; i++) {
            if (!program.arrays[i].isEmpty()) {
              program.println((i + 1) + ": " + program.arrays[i]);
            }
          } */
        }
        break;

      case EDIT:
        if (children.length == 0) {
          program.console.edit(program.getSymbol("main"));
        } else if (children[0] instanceof Identifier) {
          program.console.edit(program.getSymbol(((Identifier) children[0]).getName()));
        } else {
          program.console.edit(children[0].evalInt(evaluationContext));
        }
        break;

      case LIST: {
        program.print(program.console.getSelectedFunction().toString());
        break;
      }

      case LOAD:
        try {
          program.load(program.console.nameToReference(children[0].evalString(evaluationContext)));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        break;


      case RUN:
        program.clear(evaluationContext);

        evaluationContext.currentLine = children.length == 0 ? 0 : (int) children[0].evalDouble(evaluationContext);
        break;

      case SAVE:
        try {
          program.save(children.length == 0 ? program.reference : program.console.nameToReference(children[0].evalString(evaluationContext)));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        break;

      case TRON:
        evaluationContext.control.setTrace(true);
        break;
      case TROFF:
        evaluationContext.control.setTrace(false);
        break;

    }
    return null;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    if (children.length == 0) {
      appendLinked(asb, kind.name(), errors);
    } else {
      appendLinked(asb, kind.name() + " ", errors);
      children[0].toString(asb, errors, preferAscii);
      for (int i = 1; i < children.length; i++) {
        asb.append(", ");
        children[i].toString(asb, errors, preferAscii);
      }
    }
  }

}
