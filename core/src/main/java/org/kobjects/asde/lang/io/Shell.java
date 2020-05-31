package org.kobjects.asde.lang.io;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.annotatedtext.Annotations;
import org.kobjects.annotatedtext.Section;
import org.kobjects.annotatedtext.Text;
import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.classifier.StaticProperty;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.help.HelpGenerator;
import org.kobjects.asde.lang.node.HasProperty;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.node.Path;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.program.ProgramControl;
import org.kobjects.asde.lang.statement.DeclarationStatement;
import org.kobjects.asde.lang.statement.Statement;
import org.kobjects.asde.lang.runtime.StartStopListener;
import org.kobjects.asde.lang.function.UserFunction;
import org.kobjects.asde.lang.statement.VoidStatement;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.expressionparser.Tokenizer;
import org.kobjects.asde.lang.function.FunctionType;

import java.io.IOException;
import java.util.List;

public class Shell {
  final Program program;
  final public ProgramControl mainControl;
  public final ProgramControl shellControl;

  enum ShellCommand {
    DEF, LOAD, SAVE, EDIT, LIST, DELETE, HELP
  }


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

            UserFunction currentFunction = program.console.getSelectedFunction();

            List<Statement> parsed = program.parser.parseStatementList(tokenizer, currentFunction);
            int targetLine = (int) Math.ceil(lineNumber);
            boolean replace = lineNumber == targetLine;
            for (Statement statement : parsed) {
              if (replace) {
                currentFunction.setLine(targetLine, statement);
                replace = false;
              } else {
                currentFunction.insertLine(targetLine, statement);
              }
              targetLine++;
            }

            // Line added, done here.
            program.console.prompt();
            return;
          }
          // Not
          tokenizer = program.parser.createTokenizer(line);
          tokenizer.nextToken();
        }
        // Fall-through intended
      default:

        if (tokenizer.currentType == Tokenizer.TokenType.IDENTIFIER) {
          for (ShellCommand shellCommand : ShellCommand.values()) {
            if (tokenizer.currentValue.equals(shellCommand.name().toLowerCase())) {
              tokenizer.consumeIdentifier();
              program.console.print(line + "\n");
              processShellCommand(tokenizer, shellCommand);
              return;
            }
          }
        }
        List<Statement> statements = program.parser.parseStatementList(tokenizer, null);

        for (int i = 0; i < statements.size(); i++) {
          Node node = statements.get(i);
          if (node instanceof DeclarationStatement) {
            DeclarationStatement declaration = (DeclarationStatement) node;
            synchronized (program) {
              program.mainModule.putProperty(StaticProperty.createWithInitializer(
                  program.mainModule,
                  declaration.kind == DeclarationStatement.Kind.MUT,
                  declaration.getVarName(),
                  declaration.children[0]));
            }
          }
        }

        UserFunction wrapper = new UserFunction(program, new FunctionType(Types.VOID));

        for (Statement statement : statements) {
          wrapper.appendStatement(statement);
        }

        ValidationContext validationContext = ValidationContext.validateShellInput(wrapper);
        System.out.println("init deps: " + validationContext.initializationDependencies);

        AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
        wrapper.toString(asb, "", false, validationContext.errors);
        asb.append("\n");
        program.console.print(asb.build());

        if (!validationContext.hasAnyErrors()) {
          shellControl.initializeAndRunShellCode(wrapper, mainControl, validationContext.initializationDependencies);
        } else if (validationContext.errors.isEmpty()) {
          program.console.print("Errors in dependencies.");
        } else {
          for (Exception exception : validationContext.errors.values()) {
            AnnotatedStringBuilder sb = new AnnotatedStringBuilder();
            sb.append(" - ");
            sb.append(Format.exceptionToString(exception));
            sb.append("\n");
            program.console.print(sb.build());
          }
        }
    }
  }

  Property tryConsumeProperty(Tokenizer tokenizer) {
    if (tokenizer.currentType != Tokenizer.TokenType.IDENTIFIER) {
      return null;
    }
    Classifier classifier = program.mainModule;

    Property currentProperty;
    do {
      String identifier = tokenizer.consumeIdentifier();
      currentProperty = classifier.getProperty(identifier);
      if (!(currentProperty.getType() instanceof Classifier)) {
        break;
      }
      classifier = (Classifier) currentProperty.getType();
    } while (tokenizer.tryConsume("."));

    return currentProperty;
  }

  void processShellCommand(Tokenizer tokenizer, ShellCommand shellCommand) {
    switch (shellCommand) {
      case DEF:
        processDef(tokenizer);
        break;

      case DELETE:
        if (tokenizer.currentType != Tokenizer.TokenType.NUMBER) {
          throw new RuntimeException("Line Number expected for delete.");
        }
        program.console.delete(Integer.parseInt(tokenizer.currentValue));
        tokenizer.nextToken();
        break;

      case EDIT:
        if (tokenizer.currentType == Tokenizer.TokenType.NUMBER) {
          program.console.edit(Integer.parseInt(tokenizer.currentValue));
          tokenizer.nextToken();
        } else {
          Property target = tryConsumeProperty(tokenizer);
          if (target == null) {
            throw new RuntimeException("line number or identifier expected for edit.");
          }
        }
        break;

      case HELP:
        Property property;
        if (tokenizer.currentType == Tokenizer.TokenType.EOF) {
          property = null;
        } else {
          Node node = program.parser.expressionParser.parse(tokenizer);
          UserFunction wrapper = new UserFunction(program, new FunctionType(Types.VOID));
          wrapper.appendStatement(new VoidStatement(node));
          ValidationContext.validateShellInput(wrapper);
          if (node instanceof HasProperty) {
            property = ((HasProperty) node).getResolvedProperty();
          } else {
            property = null;
          }
        }
        Text helpText = HelpGenerator.renderHelp(program, property);

        program.console.print("\n");
        program.console.print(helpText.title);
        program.console.print("\n");
        for (Section section : helpText.sections) {
          program.console.print("\n");
          program.console.print(section.text);
          program.console.print("\n");
        }
        break;

      case LOAD:
        if (tokenizer.currentType != Tokenizer.TokenType.STRING) {
          throw new RuntimeException("File (string) parameter expected for load.");
        }
        try {
          program.load(program.console.nameToReference(tokenizer.currentValue));
          tokenizer.nextToken();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        break;

      case LIST: {
        AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
        Property target = tryConsumeProperty(tokenizer);
        if (target == null) {
          program.mainModule.toString(asb, "", false, false);
        } else {
          target.toString(asb, "", true, false);
        }
        program.console.print(asb.build());
        break;
      }

      case SAVE:
        try {
          ProgramReference reference;
          switch (tokenizer.currentType) {
            case EOF:
              reference = program.reference;
              break;
            case STRING:
              reference = program.console.nameToReference(tokenizer.currentValue);
              tokenizer.nextToken();
              break;
            default:
              throw new RuntimeException("No parameter or filename (string) parameter expected for save.");
          }
          program.save(reference);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        break;

    }
    if (tokenizer.currentType != Tokenizer.TokenType.EOF) {
      throw new RuntimeException("Leftover token: " + tokenizer);
    }
    program.console.prompt();
  }


  void processDef(Tokenizer tokenizer) {
    String name = tokenizer.consumeIdentifier();

    Classifier target = program.mainModule;
    while (tokenizer.tryConsume(".")) {
      target = (Classifier) target.getProperty(name).getStaticValue();
      name = tokenizer.consumeIdentifier();
    }

    FunctionType functionType = program.parser.parseFunctionSignature(tokenizer, null);
    UserFunction function = new UserFunction(program, functionType);
    if (tokenizer.tryConsume(":")) {
      for (Statement statement : program.parser.parseStatementList(tokenizer, function)) {
        function.appendStatement(statement);
      }
    }
    Property property = StaticProperty.createMethod(target, name, function);
    target.putProperty(property);
    program.console.selectProperty(property);
  }


}
