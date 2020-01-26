package org.kobjects.asde.lang.parser;


import org.kobjects.asde.lang.classifier.ClassImplementation;
import org.kobjects.asde.lang.classifier.InterfaceImplementation;
import org.kobjects.asde.lang.classifier.InterfacePropertyDescriptor;
import org.kobjects.asde.lang.function.FunctionImplementation;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.statement.AbstractDeclarationStatement;
import org.kobjects.asde.lang.statement.BlockStatement;
import org.kobjects.asde.lang.statement.Statement;
import org.kobjects.asde.lang.statement.UnparseableStatement;
import org.kobjects.asde.lang.function.CodeLine;
import org.kobjects.asde.lang.function.Types;
import org.kobjects.expressionparser.ExpressionParser;
import org.kobjects.expressionparser.Tokenizer;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.FunctionTypeImpl;
import org.kobjects.typesystem.Type;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProgramParser {


  final StatementParser statementParser;
  final Program program;

  public ProgramParser(Program program) {
    this.program = program;
    this.statementParser = program.parser;
  }

  public void parseProgram(BufferedReader reader) throws IOException {
    FunctionImplementation currentFunction = null;
    ClassImplementation currentClass = null;
    ArrayList<String> lines = new ArrayList<>();
    boolean legacyMode;
    int depth = 0;
    int lineNumber = 0;

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
          program.setDeclaration(className, new ClassImplementation(program));
        } else if (line.startsWith("interface ")) {
          int cut = line.lastIndexOf(':');
          String interfaceName = line.substring(10, cut).trim();
          System.out.println("interface forward declaration: '" + interfaceName + "'");
          program.setDeclaration(interfaceName, new InterfaceImplementation(program));
        }
        line = reader.readLine();
      }
    }
    ArrayList<Exception> exceptions = new ArrayList<>();

    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      System.out.println("Parsing: '" + line + "'; depth: " + depth);
      try {
        Tokenizer tokenizer = statementParser.createTokenizer(line);
        tokenizer.nextToken();
        if (currentFunction != null) {
          if (tokenizer.currentType == Tokenizer.TokenType.NUMBER) {
            lineNumber = (int) Double.parseDouble(tokenizer.currentValue);
            tokenizer.nextToken();
          } else {
            lineNumber+=2;
          }
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
            lineNumber = 0;
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
          FunctionType functionType = parseFunctionSignature(tokenizer, parameterNames);
          currentFunction = new FunctionImplementation(program, functionType, parameterNames.toArray(new String[0]));
          if (currentClass != null) {
            currentClass.setMethod(functionName, currentFunction);
          } else if (functionName.equals("main")) {
            // Implicitly discarding what we have just created...
            currentFunction = program.main;
          } else {
            program.setDeclaration(functionName, currentFunction);
          }
        } else if (tokenizer.tryConsume("class")) {
          String className = tokenizer.consumeIdentifier();
          currentClass = (ClassImplementation) program.getSymbol(className).getValue();
          if (!tokenizer.tryConsume(":")) {
            throw new RuntimeException("':' expected.");
          }
        } else if (tokenizer.tryConsume("interface")) {
          String interfaceName = tokenizer.consumeIdentifier();
          InterfaceImplementation currentInterface = (InterfaceImplementation) (program.getSymbol(interfaceName)).getValue();
          if (!tokenizer.tryConsume(":")) {
            throw new RuntimeException("':' expected.");
          }

          i = parseInterface(currentInterface, lines, i);

        } else if (!tokenizer.tryConsume("")) {
          AbstractDeclarationStatement declaration = statementParser.parseDeclaration(tokenizer, currentClass != null);
          if (currentClass != null) {
            currentClass.processDeclaration(declaration);
          } else {
            program.setPersistentInitializer(declaration.getVarName(), declaration);
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


  private int parseInterface(InterfaceImplementation interfaceImplementation, List<String> lines, int index) {
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
        FunctionType functionType = parseFunctionSignature(tokenizer, parameterNames);
        interfaceImplementation.addProperty(functionName, functionType);
      } else {
        Type type = statementParser.parseType(tokenizer);
        String name = tokenizer.consumeIdentifier();
        interfaceImplementation.addProperty(name, type);
      }
    }
    return index;
  }


  private Type[] parseParameterList(Tokenizer tokenizer, ArrayList<String> parameterNames) {
    tokenizer.consume("(");
    ArrayList<Type> parameterTypes = new ArrayList<>();
    while (!tokenizer.tryConsume(")")) {
      Type parameterType = statementParser.parseType(tokenizer);
      parameterTypes.add(parameterType);
      String parameterName = tokenizer.consumeIdentifier();
      parameterNames.add(parameterName);

      if (!tokenizer.tryConsume(",")) {
        if (tokenizer.tryConsume(")")) {
          break;
        }
        throw new RuntimeException("',' or ')' expected.");
      }
    }
    return parameterTypes.toArray(Type.EMPTY_ARRAY);
  }

  private FunctionType parseFunctionSignature(Tokenizer tokenizer, ArrayList<String> parameterNames) {
    Type[] parameterTypes = parseParameterList(tokenizer, parameterNames);

    Type returnType = tokenizer.tryConsume("->") ? statementParser.parseType(tokenizer) : Types.VOID;

    if (!tokenizer.tryConsume(":")) {
      throw new RuntimeException("':' expected.");
    }

    return new FunctionTypeImpl(returnType, parameterTypes);
  }


}
