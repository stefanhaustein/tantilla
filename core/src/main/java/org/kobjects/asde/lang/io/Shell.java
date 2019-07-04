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
import org.kobjects.asde.lang.FunctionImplementation;
import org.kobjects.asde.lang.type.CodeLine;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.expressionparser.ExpressionParser;
import org.kobjects.typesystem.FunctionTypeImpl;

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

                FunctionImplementation wrapper = new FunctionImplementation(program, new FunctionTypeImpl(Types.VOID));
                wrapper.setLine(codeLine);
                ProgramValidationContext programValidationContext = new ProgramValidationContext(program);
                FunctionValidationContext functionValidationContext = new FunctionValidationContext(programValidationContext, FunctionValidationContext.ResolutionMode.INTERACTIVE, wrapper);
                wrapper.validate(functionValidationContext);

                if (functionValidationContext.errors.size() > 0) {
                    throw new ValidationException(codeLine, functionValidationContext.errors);
                }
                AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
                asb.append(codeLine.toString(), Annotations.ACCENT_COLOR);
                asb.append("\n");
                program.console.print(asb.build());

                shellControl.runStatementsAsync(wrapper, mainControl, resultConsumer);
                break;
        }
    }



}
