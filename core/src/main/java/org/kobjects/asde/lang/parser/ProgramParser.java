package org.kobjects.asde.lang.parser;


import org.kobjects.asde.lang.classifier.AdapterType;
import org.kobjects.asde.lang.classifier.ClassType;
import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.classifier.Trait;
import org.kobjects.asde.lang.classifier.GenericProperty;
import org.kobjects.asde.lang.classifier.TraitProperty;
import org.kobjects.asde.lang.function.UserFunction;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.statement.BlockStatement;
import org.kobjects.asde.lang.statement.Statement;
import org.kobjects.asde.lang.statement.UnparseableStatement;
import org.kobjects.expressionparser.Tokenizer;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.type.Type;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProgramParser {


  final StatementParser statementParser;
  final Program program;

  public ProgramParser(Program program) {
    this.program = program;
    this.statementParser = program.parser;
  }

  public void parseProgram(BufferedReader reader) throws IOException {
    UserFunction currentFunction = null;
    Classifier currentClassifier = null;
    ArrayList<String> lines = new ArrayList<>();
    boolean legacyMode;
    int depth = 0;

    {
      String line = reader.readLine();
      legacyMode = (line != null && !(line + ' ').startsWith("ASDE "));
      if (!legacyMode) {
        line = reader.readLine();
      }
      while (line != null) {
        line = line.trim();
        lines.add(line);
        if (line.startsWith("class ")) {
          int cut = line.lastIndexOf(':');
          String className = line.substring(6, cut).trim();
          System.out.println("class forward declaration: '" + className + "'");
          synchronized (program) {
            program.mainModule.putProperty(GenericProperty.createStatic(program.mainModule, className, new ClassType(program)));
          }
        } else if (line.startsWith("trait ")) {
          int cut = line.lastIndexOf(':');
          String interfaceName = line.substring("trait".length() + 1, cut).trim();
          System.out.println("trait forward declaration: '" + interfaceName + "'");
          synchronized (program) {
            program.mainModule.putProperty(GenericProperty.createStatic(program.mainModule, interfaceName, new Trait(program)));
          }
        }
        line = reader.readLine();
      }
    }
    ArrayList<Exception> exceptions = new ArrayList<>();

    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      System.out.println("Parsing #" + (i+1) + " depth: " + depth + ": '" + line + "'");
      try {
        Tokenizer tokenizer = statementParser.createTokenizer(line);
        tokenizer.nextToken();
        if (currentFunction != null) {
          int pos = tokenizer.currentPosition;
          try {
            List<Statement> statements = statementParser.parseStatementList(tokenizer, currentFunction);
            for (int j = 0; j < statements.size(); j++) {
              Statement statement = statements.get(j);
              if (statement instanceof BlockStatement) {
                depth++;
              }
              // might be both!
              if (statement.closesBlock()) {
                depth--;
                if (depth < 0) {
                  if (j != statements.size() -1) {
                    throw new RuntimeException("Unexpected end");
                  }
                  continue;
                }
              }
              currentFunction.appendStatement(statement);
            }
          } catch (Exception e) {
            currentFunction.appendStatement(new UnparseableStatement(line.substring(pos), e));
          }
          if (depth < 0) {
            depth = 0;
            currentFunction = null;
          }
        } else if (tokenizer.tryConsume("end")) {
          if (currentClassifier != null) {
            currentClassifier = null;
          } else {
            throw new RuntimeException("Unexpected end");
          }
        } else if (tokenizer.tryConsume("def")) {
          String functionName = tokenizer.consumeIdentifier();
          program.console.updateProgress("Parsing function " + functionName);
          FunctionType functionType = statementParser.parseFunctionSignature(tokenizer, currentClassifier);
          if (!tokenizer.tryConsume(":")) {
            throw new RuntimeException("':' expected.");
          }
          currentFunction = new UserFunction(program, functionType);
          if (currentClassifier != null) {
            currentClassifier.putProperty(GenericProperty.createMethod(currentClassifier, functionName, currentFunction));
          } else if (functionName.equals("main")) {
            // Implicitly discarding what we have just created...
            currentFunction = program.main;
          } else {
            synchronized (program) {
              program.mainModule.putProperty(GenericProperty.createStatic(program.mainModule, functionName, currentFunction));
            }
          }
        } else if (tokenizer.tryConsume("class")) {
          String className = tokenizer.consumeIdentifier();
          tokenizer.consume(":");
          currentClassifier = (ClassType) program.mainModule.getProperty(className).getStaticValue();
          // currentClass = (UserClass) program.getSymbol(className).getStaticValue();
        } else if (tokenizer.tryConsume("impl")) {
          ClassType classType = (ClassType) statementParser.parseType(tokenizer);
          tokenizer.consume("as");
          Trait trait = (Trait) statementParser.parseType(tokenizer);
          tokenizer.consume(":");
          currentClassifier = new AdapterType(classType, trait);
          synchronized (program) {
            program.mainModule.putProperty(GenericProperty.createStatic(program.mainModule, currentClassifier.toString(), currentClassifier));
          }
        } else if (tokenizer.tryConsume("trait")) {
          String interfaceName = tokenizer.consumeIdentifier();
          Trait currentTrait = (Trait) program.mainModule.getProperty(interfaceName).getStaticValue();
          //         Trait currentTrait = (Trait) (program.getSymbol(interfaceName)).getStaticValue();
          tokenizer.consume(":");
          i = parseTrait(currentTrait, lines, i);
        } else if (!tokenizer.tryConsume("")) {
          boolean isConst = tokenizer.tryConsume("const");
          boolean mutable = !isConst && tokenizer.tryConsume("mut");
          String name = tokenizer.consumeIdentifier();
          if (currentClassifier != null) {
            if (tokenizer.tryConsume("=")) {
              Node initilaizer = statementParser.expressionParser.parse(tokenizer);
              currentClassifier.putProperty(GenericProperty.createWithInitializer(currentClassifier, !isConst, mutable, name, initilaizer));
            } else if (tokenizer.tryConsume(":")) {
              Type type = statementParser.parseType(tokenizer);
              currentClassifier.putProperty(GenericProperty.createUninitialized(
                  currentClassifier,
                  mutable,
                  name,
                  type));
            } else {
              throw new RuntimeException("= or : expected after property name");
            }
          } else {
            tokenizer.consume("=");
            Node initilaizer = statementParser.expressionParser.parse(tokenizer);
            // mutable is (currently?) redundant here...
            program.mainModule.putProperty(GenericProperty.createWithInitializer(program.mainModule, false, !isConst, name, initilaizer));
          }
          // Push down?
          if (tokenizer.currentType != Tokenizer.TokenType.EOF) {
            throw tokenizer.exception("Leftover input", null);
          }
        }
      } catch (Exception e) {
        throw new RuntimeException("Error while parsing line: " + line, e);
      }
    }


    if (exceptions.size() > 0) {
      throw new RuntimeException("Loading errors (see console): " + exceptions);
    }
  }




  private int parseTrait(Trait trait, List<String> lines, int index) {
    while (true) {
      String line = lines.get(++index);
      Tokenizer tokenizer = statementParser.createTokenizer(line);
      tokenizer.nextToken();
      if (tokenizer.tryConsume("end")) {
        break;
      }
      if (tokenizer.tryConsume("def")) {
        String functionName = tokenizer.consumeIdentifier();
        FunctionType functionType = statementParser.parseFunctionSignature(tokenizer, trait);
        trait.putProperty(TraitProperty.create(trait, functionName, functionType));
      } else {
       throw new RuntimeException("def or end expected.");
      }
    }
    return index;
  }





}
