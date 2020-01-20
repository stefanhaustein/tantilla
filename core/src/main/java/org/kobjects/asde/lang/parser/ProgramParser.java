package org.kobjects.asde.lang.parser;


import org.kobjects.asde.lang.classifier.ClassImplementation;
import org.kobjects.asde.lang.classifier.InterfaceImplementation;
import org.kobjects.asde.lang.classifier.InterfacePropertyDescriptor;
import org.kobjects.asde.lang.function.FunctionImplementation;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.statement.AbstractDeclarationStatement;
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

  private static final String[] KEYWORDS = {
    "AND", "DATA", "DIM", "ELSE", "FOR", "GOTO", "GOSUB", "IF", "LET", "NEXT", "ON", "OR", "PRINT", "REM", "RESTORE", "STEP", "STOP", "THEN", "TO"
  };


  private static String preprocessLegacyIdentifiers(String line) {
    StringBuilder sb = new StringBuilder();
    boolean inQuote = false;
    int len = line.length();
    for (int i = 0; i < len; i++) {
      char c = line.charAt(i);
      if (c == '"' || inQuote) {
        inQuote = c == '"' ? !inQuote : true;
        sb.append(c);
      } else if (c == '\'') {
        sb.append(line.substring(i));
        break;
      } else if (c >= 'A' && c <= 'T') {
        int originalI = i;
        for (String keyword: KEYWORDS) {
          if (line.startsWith(keyword, i)) {
            if (i > 0 && line.charAt(i - 1) != ' ') {
              sb.append(' ');
            }
            sb.append(keyword);
            if (i + keyword.length() < len && line.charAt(i + keyword.length()) != ' ') {
              sb.append(' ');
            }
            i += keyword.length() - 1;
            if (keyword.equals("REM")) {
              sb.append(line.substring(i + 1));
              return sb.toString();
            }
            break;
          }
        }
        if (i == originalI) {
          sb.append(c);
        }
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }


  final StatementParser statementParser;
  final Program program;

  public ProgramParser(Program program) {
    this.program = program;
    this.statementParser = program.parser;
  }

  public void parseProgram(BufferedReader reader) throws IOException {
    FunctionImplementation currentFunction = program.main;
    ClassImplementation currentClass = null;
    ArrayList<String> lines = new ArrayList<>();
    boolean legacyMode;

    {
      String line = reader.readLine();
      legacyMode = (line != null && !(line + ' ').startsWith("ASDE "));
      if (!legacyMode) {
        line = reader.readLine();
      }
      while (line != null) {
        line = line.trim();
        if (legacyMode) {
          line = preprocessLegacyIdentifiers(line);
          System.out.println("Preprocessed: '" + line + "'");
        }
        lines.add(line);
        String upper = line.toUpperCase();
        if (upper.startsWith("CLASS ")) {
          String className = line.substring(6).trim();
          program.setDeclaration(className, new ClassImplementation(program));
        } else if (upper.startsWith("INTERFACE ")) {
          String interfaceName = line.substring(10).trim();
          program.setDeclaration(interfaceName, new InterfaceImplementation(program));
        }
        line = reader.readLine();
      }
    }
    ArrayList<Exception> exceptions = new ArrayList<>();

    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      System.out.println("Parsing: '" + line + "'");
      try {
        Tokenizer tokenizer = statementParser.createTokenizer(line);
        tokenizer.nextToken();
        if (tokenizer.currentType == Tokenizer.TokenType.NUMBER) {
          int lineNumber = (int) Double.parseDouble(tokenizer.currentValue);
          tokenizer.nextToken();
          int pos = tokenizer.currentPosition;
          try {
            List<? extends Node> statements = statementParser.parseStatementList(tokenizer, currentFunction);
            currentFunction.setLine(new CodeLine(lineNumber, statements));
          } catch (Exception e) {
            currentFunction.setLine(new CodeLine(lineNumber, Collections.singletonList(new UnparseableStatement(line.substring(pos), e))));
          }
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
          currentClass = (ClassImplementation) program.getSymbol(className).getValue();
        } else if (tokenizer.tryConsume("INTERFACE")) {
          String interfaceName = tokenizer.consumeIdentifier();
          InterfaceImplementation currentInterface = (InterfaceImplementation) (program.getSymbol(interfaceName)).getValue();

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
      if (tokenizer.tryConsume("END")) {
        break;
      }
      if (tokenizer.tryConsume("FUNCTION") || tokenizer.tryConsume("SUB")) {
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

    return new FunctionTypeImpl(returnType, parameterTypes);
  }


}
