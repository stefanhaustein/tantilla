package org.kobjects.asde.lang;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.annotatedtext.Annotations;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.expressionparser.ExpressionParser;

import java.util.List;

public class Shell {


    final Program program;
    final public ProgramControl mainInterpreter ;
    public final ProgramControl shellInterpreter ;

    CallableUnit currentFunction;

    public Shell(Program program) {
        this.program = program;
        mainInterpreter = new ProgramControl(program);
        shellInterpreter = new ProgramControl(program);
        currentFunction = program.main;
    }

    public void setCurrentFunction(CallableUnit callableUnit) {
        currentFunction = callableUnit;
    }


    public void enter(String line, Consumer<Object> resultConsumer) {
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
                    if (tokenizer.currentType == ExpressionParser.Tokenizer.TokenType.IDENTIFIER || "?".equals(tokenizer.currentValue)) {
                        currentFunction.setLine(lineNumber, new CodeLine(program.parser.parseStatementList(tokenizer)));
                        program.console.sync(true);
                        if (program.reference.urlWritable) {
                            try {
                                program.save(program.reference);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
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
                List<? extends Node> statements = program.parser.parseStatementList(tokenizer);
                program.processDeclarations(statements);

                    /*TextView inputView = new EmojiTextView(this);
                    inputView.setText(new CodeLine(statements).toString());
                    inputView.setTextColor(colors.accent);
                    inputView.setTypeface(Typeface.MONOSPACE);

                    if (lineFeedPending) {
                        print("");
                    }

                    outputView.addView(inputView);
                    postScrollIfAtEnd();*/

                AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
                asb.append(new CodeLine(statements).toString(), Annotations.ACCENT_COLOR);
                asb.append("\n");
                program.console.print(asb.build());

                shellInterpreter.runStatementsAsync(statements, mainInterpreter, resultConsumer);
                break;
        }
    }



}
