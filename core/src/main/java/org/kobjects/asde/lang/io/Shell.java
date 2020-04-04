package org.kobjects.asde.lang.io;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.annotatedtext.Annotations;
import org.kobjects.asde.lang.Consumer;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.program.ProgramControl;
import org.kobjects.asde.lang.statement.DeclarationStatement;
import org.kobjects.asde.lang.statement.Statement;
import org.kobjects.asde.lang.runtime.StartStopListener;
import org.kobjects.asde.lang.function.UserFunction;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.expressionparser.Tokenizer;
import org.kobjects.asde.lang.function.FunctionType;

import java.util.List;

public class Shell {
  final Program program;
  final public ProgramControl mainControl;
  public final ProgramControl shellControl;

  public Shell(Program program) {
    this.program = program;
    mainControl = new ProgramControl(program);
    shellControl = new ProgramControl(program);
    shellControl.addStartStopListener(new StartStopListener() {
      @Override
      public void programStarted() {
      }

      @Override
      public void programAborted(Exception cause) {
        if (cause != null) {
          program.console.showError(null, cause);
        }
      }

      @Override
      public void programPaused() {
      }

      @Override
      public void programEnded() {
      }
    });

  }


  public void enter(String line) {
    Property currentFunction = program.console.getSelectedFunction().getDeclaringSymbol();
    if (line.isEmpty()) {
      program.console.prompt();
      return;
    }

    Tokenizer tokenizer = program.parser.createTokenizer(line);
    tokenizer.nextToken();
    switch (tokenizer.currentType) {
      case EOF:
        break;
      case NUMBER:
        if (!tokenizer.currentValue.startsWith("#")) {
          double lineNumber = Double.parseDouble(tokenizer.currentValue);
          tokenizer.nextToken();
          if (tokenizer.currentType == Tokenizer.TokenType.IDENTIFIER
              || "?".equals(tokenizer.currentValue)) {

            List<Statement> parsed = program.parser.parseStatementList(tokenizer, (UserFunction) currentFunction.getStaticValue());
            int targetLine = (int) Math.ceil(lineNumber);
            boolean replace = lineNumber == targetLine;
            for (Statement statement : parsed) {
              if (replace) {
                ((UserFunction) currentFunction.getStaticValue()).setLine(targetLine, statement);
                replace = false;
              } else {
                ((UserFunction) currentFunction.getStaticValue()).insertLine(targetLine, statement);
              }
              targetLine++;
            }

            // Line added, done here.
            break;
          }
          // Not
          tokenizer = program.parser.createTokenizer(line);
          tokenizer.nextToken();
        }
        // Fall-through intended
      default:
        // TODO: Check for def, class, trait, const

        List<Statement> statements = program.parser.parseStatementList(tokenizer, null);

        for (int i = 0; i < statements.size(); i++) {
          Node node = statements.get(i);
          if (node instanceof DeclarationStatement) {
            DeclarationStatement declaration = (DeclarationStatement) node;
            program.setPersistentInitializer(declaration.getVarName(), declaration);
          }
        }

        UserFunction wrapper = new UserFunction(program, new FunctionType(Types.VOID));

        for (Statement statement : statements) {
          wrapper.appendStatement(statement);
        }

        ValidationContext validationContext = ValidationContext.createForFunction(wrapper);
        validationContext.validate();
        System.out.println("init deps: " + validationContext.initializationDependencies);

        if (validationContext.errors.size() > 0) {
          throw new MultiValidationException(wrapper, validationContext.errors);
        }

        AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
        asb.append(wrapper.toString(), Annotations.ACCENT_COLOR);
        asb.append("\n");
        program.console.print(asb.build());

        shellControl.initializeAndRunShellCode(wrapper, mainControl, validationContext.initializationDependencies);
        break;
    }
  }



}
