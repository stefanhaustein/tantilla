package org.kobjects.asde.lang.io;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.annotatedtext.Annotations;
import org.kobjects.asde.lang.Consumer;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.asde.lang.GlobalSymbol;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.ProgramControl;
import org.kobjects.asde.lang.ProgramValidationContext;
import org.kobjects.asde.lang.event.StartStopListener;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.type.FunctionImplementation;
import org.kobjects.asde.lang.type.CodeLine;
import org.kobjects.expressionparser.ExpressionParser;

import java.util.List;

public class Shell {


    final Program program;
    final public ProgramControl mainInterpreter ;
    public final ProgramControl shellInterpreter ;

    public Shell(Program program) {
        this.program = program;
        mainInterpreter = new ProgramControl(program);
        shellInterpreter = new ProgramControl(program);
        shellInterpreter.addStartStopListener(new StartStopListener() {
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


    public void enter(String line, GlobalSymbol currentFunction, Consumer<Object> resultConsumer) {
        if (line.isEmpty()) {
            resultConsumer.accept("");
            return;
        }

        ExpressionParser.Tokenizer tokenizer = program.parser.createTokenizer(line);
        tokenizer.nextToken();
        switch (tokenizer.currentType) {
            case EOF:
                break;
            case NUMBER:
                if (!tokenizer.currentValue.startsWith("#")) {
                    int lineNumber = (int) Double.parseDouble(tokenizer.currentValue);
                    tokenizer.nextToken();
                    if (tokenizer.currentType == ExpressionParser.Tokenizer.TokenType.IDENTIFIER
                            || "?".equals(tokenizer.currentValue)) {
                        program.setLine(currentFunction, new CodeLine(lineNumber, program.parser.parseStatementList(tokenizer, (FunctionImplementation) currentFunction.getValue())));
                        // Line added, done here.
                        break;
                    }
                    // Not
                    tokenizer = program.parser.createTokenizer(line);
                    tokenizer.nextToken();
                }
                // Fall-through intended
            default:
                List<? extends Node> statements = program.parser.parseStatementList(tokenizer, null);

                CodeLine codeLine = new CodeLine(-2, statements);
                program.processDeclarations(codeLine);

                ProgramValidationContext programValidationContext = new ProgramValidationContext(program);
                FunctionValidationContext functionValidationContext = new FunctionValidationContext(programValidationContext, FunctionValidationContext.ResolutionMode.SHELL, program.main);
                int index = 0;
                for (Node node : codeLine) {
                    node.resolve(functionValidationContext, -2, index++);
                    if (functionValidationContext.errors.size() > 0) {
                        Exception exception = functionValidationContext.errors.values().iterator().next();
                        throw new RuntimeException(exception);
                    }
                }

                AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
                if (functionValidationContext.errors.size() == 0) {
                    asb.append(codeLine.toString(), Annotations.ACCENT_COLOR);
                } else {
                    // Show error like a syntax error instead?
                    codeLine.toString(asb, functionValidationContext.errors);
                }
                asb.append("\n");
                program.console.print(asb.build());

                shellInterpreter.runStatementsAsync(codeLine, mainInterpreter, resultConsumer);
                break;
        }
    }



}
