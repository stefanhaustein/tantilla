package org.kobjects.asde.lang.parser;


import org.kobjects.asde.lang.classifier.UserClass;
import org.kobjects.asde.lang.classifier.Trait;
import org.kobjects.asde.lang.classifier.UserProperty;
import org.kobjects.asde.lang.function.UserFunction;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.statement.BlockStatement;
import org.kobjects.asde.lang.statement.DeclarationStatement;
import org.kobjects.asde.lang.statement.Statement;
import org.kobjects.asde.lang.statement.UnparseableStatement;
import org.kobjects.asde.lang.type.Types;
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
    UserClass currentClass = null;
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
          System.out.println("class forward declaration: '" + className + "'");
          program.setDeclaration(className, new UserClass(program));
        } else if (line.startsWith("trait ")) {
          int cut = line.lastIndexOf(':');
          String interfaceName = line.substring("trait".length() + 1, cut).trim();
          System.out.println("trait forward declaration: '" + interfaceName + "'");
          program.setDeclaration(interfaceName, new Trait(program));
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
              // might be both!
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
          if (currentClass != null) {
            currentClass = null;
          } else {
            throw new RuntimeException("Unexpected end");
          }
        } else if (tokenizer.tryConsume("def")) {
          String functionName = tokenizer.consumeIdentifier();
          program.console.updateProgress("Parsing function " + functionName);
          ArrayList<String> parameterNames = new ArrayList();
          FunctionType functionType = parseFunctionSignature(tokenizer, parameterNames, currentClass);
          if (!tokenizer.tryConsume(":")) {
            throw new RuntimeException("':' expected.");
          }
          currentFunction = new UserFunction(program, functionType, parameterNames.toArray(new String[0]));
          if (currentClass != null) {
            currentClass.putProperty(UserProperty.createMethod(currentClass, functionName, currentFunction));
          } else if (functionName.equals("main")) {
            // Implicitly discarding what we have just created...
            currentFunction = program.main;
          } else {
            program.setDeclaration(functionName, currentFunction);
          }
        } else if (tokenizer.tryConsume("class")) {
          String className = tokenizer.consumeIdentifier();
          currentClass = (UserClass) program.mainModule.getProperty(className).getStaticValue();
          // currentClass = (UserClass) program.getSymbol(className).getStaticValue();
          if (!tokenizer.tryConsume(":")) {
            throw new RuntimeException("':' expected.");
          }
        } else if (tokenizer.tryConsume("trait")) {
          String interfaceName = tokenizer.consumeIdentifier();
          Trait currentTrait = (Trait) program.mainModule.getProperty(interfaceName).getStaticValue();
 //         Trait currentTrait = (Trait) (program.getSymbol(interfaceName)).getStaticValue();
          if (!tokenizer.tryConsume(":")) {
            throw new RuntimeException("':' expected.");
          }

          i = parseTrait(currentTrait, lines, i);

        } else if (!tokenizer.tryConsume("")) {
          if (currentClass != null) {
            tokenizer.consume("var");
            String name = tokenizer.consumeIdentifier();
            if (tokenizer.tryConsume("=")) {
              Node initilaizer = statementParser.expressionParser.parse(tokenizer);
              currentClass.putProperty(UserProperty.createWithInitializer(currentClass, name, initilaizer));
            } else if (tokenizer.tryConsume(":")) {
              Type type = statementParser.parseType(tokenizer);
              currentClass.putProperty(UserProperty.createUninitialized(
                  currentClass,
                  name,
                  type));
            } else {
              throw new RuntimeException("= or : expected after property name");
            }
          } else {
            DeclarationStatement declaration = statementParser.parseDeclaration(tokenizer);
            program.setPersistentInitializer(declaration.getVarName(), declaration);
          }
          // Push down?
          if (tokenizer.currentType != Tokenizer.TokenType.EOF) {
            throw tokenizer.exception("Leftover input", null);
          }
        }
      } catch (Exception e) {
        throw new RuntimeException("Error while parsing line: " + line, e);
      }
    }


    if (exceptions.size() > 0) {
      throw new RuntimeException("Loading errors (see console): " + exceptions);
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
        ArrayList<String> parameterNames = new ArrayList();
        FunctionType functionType = parseFunctionSignature(tokenizer, parameterNames, trait);
        trait.addProperty(functionName, functionType);
      } else if (tokenizer.tryConsume("var")) {
        String name = tokenizer.consumeIdentifier();
        tokenizer.consume(":");
        Type type = statementParser.parseType(tokenizer);
        trait.addProperty(name, type);
      } else {
        throw new RuntimeException("def or var expected!");
      }
    }
    return index;
  }


  private Type[] parseParameterList(Tokenizer tokenizer, ArrayList<String> parameterNames, Type self) {
    tokenizer.consume("(");
    ArrayList<Type> parameterTypes = new ArrayList<>();
    while (!tokenizer.tryConsume(")")) {
      String parameterName = tokenizer.consumeIdentifier();
      parameterNames.add(parameterName);
      if (parameterName.equals("self")) {
        if (parameterTypes.size() != 0) {
          throw new RuntimeException("self must be first parameter");
        }
        if (self == null) {
          throw new RuntimeException("The parameter name 'self' is reserved for class and trait methods.");
        }
        if (tokenizer.tryConsume(":")) {
          Type parameterType = statementParser.parseType(tokenizer);
          if (parameterType != self) {
            throw new RuntimeException("Type mismatch for self. Expected: " + self + " got: " + parameterType);
          }
        }
        parameterTypes.add(self);
      } else {
        tokenizer.consume(":");
        Type parameterType = statementParser.parseType(tokenizer);
        parameterTypes.add(parameterType);
      }
      if (!tokenizer.tryConsume(",")) {
        if (tokenizer.tryConsume(")")) {
          break;
        }
        throw new RuntimeException("',' or ')' expected.");
      }
    }
    return parameterTypes.toArray(Type.EMPTY_ARRAY);
  }

  private FunctionType parseFunctionSignature(Tokenizer tokenizer, ArrayList<String> parameterNames, Type self) {
    Type[] parameterTypes = parseParameterList(tokenizer, parameterNames, self);

    Type returnType = tokenizer.tryConsume("->") ? statementParser.parseType(tokenizer) : Types.VOID;

    return new FunctionType(returnType, parameterTypes);
  }


}
