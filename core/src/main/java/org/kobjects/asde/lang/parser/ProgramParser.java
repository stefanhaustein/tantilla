package org.kobjects.asde.lang.parser;


import org.kobjects.asde.lang.ClassImplementation;
import org.kobjects.asde.lang.FunctionImplementation;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.type.CodeLine;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.expressionparser.ExpressionParser;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.FunctionTypeImpl;
import org.kobjects.typesystem.Type;

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
    FunctionImplementation currentFunction = program.main;
    ClassImplementation currentClass = null;
    String line = reader.readLine();
    program.legacyMode = line != null && !(line + ' ').startsWith("ASDE ");
    if (!program.legacyMode) {
      line = reader.readLine();
    }
    while (line != null) {
      System.out.println("Parsing: '" + line + "'");

      ExpressionParser.Tokenizer tokenizer = statementParser.createTokenizer(line);
      tokenizer.nextToken();
      if (tokenizer.currentType == ExpressionParser.Tokenizer.TokenType.NUMBER) {
        int lineNumber = (int) Double.parseDouble(tokenizer.currentValue);
        tokenizer.nextToken();
        List<? extends Node> statements = statementParser.parseStatementList(tokenizer, currentFunction);
        currentFunction.setLine(new CodeLine(lineNumber, statements));
      } else if (tokenizer.tryConsume("FUNCTION") || tokenizer.tryConsume("SUB")) {
        String functionName = tokenizer.consumeIdentifier();
        program.console.updateProgress("Parsing function " + functionName);
        ArrayList<String> parameterNames = new ArrayList();
        FunctionType functionType = parseFunctionSignature(tokenizer, parameterNames);
        currentFunction = new FunctionImplementation(program, functionType, parameterNames.toArray(new String[0]));
        if (currentClass != null) {
          currentClass.setMethod(functionName, currentFunction);
        } else {
          program.setDeclaration(functionName, currentFunction);
        }
      } else if (tokenizer.tryConsume("END")) {
        if (currentFunction != program.main) {
          currentFunction = program.main;
        } else if (currentClass != null) {
          currentClass = null;
        }
      } else if (tokenizer.tryConsume("CLASS")) {
        String className = tokenizer.consumeIdentifier();
        currentClass = new ClassImplementation(program);
        program.setDeclaration(className, currentClass);
      } else if (!tokenizer.tryConsume("")) {
        List<? extends Node> statements = statementParser.parseStatementList(tokenizer, null);
        CodeLine codeLine = new CodeLine(-2, statements);
        if (currentClass != null) {
          currentClass.processDeclarations(codeLine);
        } else {
          program.processStandaloneDeclarations(codeLine);
        }
      }
      line = reader.readLine();
    }
  }


  private Type[] parseParameterList(ExpressionParser.Tokenizer tokenizer, ArrayList<String> parameterNames) {
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
    return parameterTypes.toArray(Type.EMTPY_ARRAY);
  }

  private FunctionType parseFunctionSignature(ExpressionParser.Tokenizer tokenizer, ArrayList<String> parameterNames) {
    Type[] parameterTypes = parseParameterList(tokenizer, parameterNames);

    Type returnType = tokenizer.tryConsume("->") ? statementParser.parseType(tokenizer) : Types.VOID;

    return new FunctionTypeImpl(returnType, parameterTypes);
  }


}
