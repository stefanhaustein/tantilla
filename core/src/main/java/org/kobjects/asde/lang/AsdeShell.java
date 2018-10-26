package org.kobjects.asde.lang;

import org.kobjects.asde.lang.node.Node;
import org.kobjects.expressionparser.ExpressionParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class AsdeShell {

    /**
     * Returns true if the line was "interactive" and a "ready" prompt should be displayed.
     */
    public static boolean processInputLine(ProgramControl control, String line) {
        Program program = control.program;
        ExpressionParser.Tokenizer tokenizer = program.parser.createTokenizer(line);

        tokenizer.nextToken();
        switch (tokenizer.currentType) {
            case EOF:
                return false;
            case NUMBER:
                int lineNumber = (int) Double.parseDouble(tokenizer.currentValue);
                tokenizer.nextToken();
                if (tokenizer.currentType == ExpressionParser.Tokenizer.TokenType.EOF) {
                    program.main.setLine(lineNumber, null);
                } else {
                    program.main.setLine(lineNumber, new CodeLine(program.parser.parseStatementList(tokenizer)));
                }
                return false;
            default:
                List<? extends Node> statements = program.parser.parseStatementList(tokenizer);
                control.runStatementsAsync(statements, control);
                return true;
        }
    }

  public static void main(String[] args) throws IOException {
    final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    Program program = new Program(new Console() {
        @Override
        public void print(String s) {
            System.out.print(s);
        }

        @Override
        public String read() {
            try {
                return reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public File getProgramStoragePath() {
            throw new RuntimeException("NYI");
        }

        @Override
        public void programNameChangedTo(String name) {
            // Ignore
        }

        @Override
        public void clearOutput() {

        }

        @Override
        public void clearCanvas() {

        }


        @Override
        public void trace(CallableUnit function, int lineNumber) {
            // TBD
        }
    });

    System.out.println("  **** EXPRESSION PARSER BASIC DEMO V1 ****\n");
    System.out.println("  " + (Runtime.getRuntime().totalMemory() / 1024) + "K SYSTEM  "
        + Runtime.getRuntime().freeMemory() + " BASIC BYTES FREE\n");

    boolean prompt = true;

    ProgramControl control = new ProgramControl(program);
    while (true) {
      if (prompt) {
        System.out.println("\nREADY.");
      }
      String line = reader.readLine();
      if (line == null) {
        break;
      }
      prompt = true;
      try {
        prompt = processInputLine(control, line);
      } catch (ExpressionParser.ParsingException e) {
        char[] fill = new char[e.start + 1];
        Arrays.fill(fill, ' ');
        System.out.println(new String(fill) + '^');
        System.out.println("?SYNTAX ERROR: " + e.getMessage());
        program.lastException = e;
      } catch (Exception e) {
        System.out.println("\nERROR in " + control.rootInterprter.currentLine + ':'
            + control.rootInterprter.currentIndex + ": " + e.getMessage());
        System.out.println("\nREADY.");
        program.lastException = e;
      }
    }
  }
}
