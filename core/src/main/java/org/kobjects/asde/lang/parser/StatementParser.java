package org.kobjects.asde.lang.parser;

import org.kobjects.asde.lang.array.ArrayType;
import org.kobjects.asde.lang.function.FunctionImplementation;
import org.kobjects.asde.lang.program.GlobalSymbol;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.node.Apply;
import org.kobjects.asde.lang.node.Group;
import org.kobjects.asde.lang.node.NegOperator;
import org.kobjects.asde.lang.node.Path;
import org.kobjects.asde.lang.node.RelationalOperator;
import org.kobjects.asde.lang.statement.AbstractDeclarationStatement;
import org.kobjects.asde.lang.statement.AssignStatement;
import org.kobjects.asde.lang.node.AssignableNode;
import org.kobjects.asde.lang.statement.Command;
import org.kobjects.asde.lang.statement.DebuggerStatement;
import org.kobjects.asde.lang.statement.DefStatement;
import org.kobjects.asde.lang.statement.DimStatement;
import org.kobjects.asde.lang.statement.ElseStatement;
import org.kobjects.asde.lang.statement.EndIfStatement;
import org.kobjects.asde.lang.statement.EndStatement;
import org.kobjects.asde.lang.statement.ForStatement;
import org.kobjects.asde.lang.statement.GotoStatement;
import org.kobjects.asde.lang.statement.IfStatement;
import org.kobjects.asde.lang.statement.IoStatement;
import org.kobjects.asde.lang.statement.DeclarationStatement;
import org.kobjects.asde.lang.node.Literal;
import org.kobjects.asde.lang.statement.NextStatement;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.node.MathOperator;
import org.kobjects.asde.lang.statement.OnStatement;
import org.kobjects.asde.lang.statement.ReadStatement;
import org.kobjects.asde.lang.statement.RemStatement;
import org.kobjects.asde.lang.statement.FunctionReturnStatement;
import org.kobjects.asde.lang.statement.LegacyStatement;
import org.kobjects.asde.lang.node.Identifier;
import org.kobjects.asde.lang.statement.UninitializedField;
import org.kobjects.asde.lang.statement.VoidStatement;
import org.kobjects.asde.lang.function.CodeLine;
import org.kobjects.asde.lang.function.Types;
import org.kobjects.expressionparser.ExpressionParser;
import org.kobjects.typesystem.FunctionTypeImpl;
import org.kobjects.typesystem.Type;

import java.util.ArrayList;
import java.util.List;

public class StatementParser {
  final AsdeExpressionParser expressionParser;
  final Program program;

  public StatementParser(Program program) {
    this.program = program;

    expressionParser = new AsdeExpressionParser(program);
  }

  public ExpressionParser.Tokenizer createTokenizer(String line) {
    return expressionParser.createTokenizer(line);
  }

  public Node parseExpression(String unparsed) {
    ExpressionParser.Tokenizer tokenizer = expressionParser.createTokenizer(unparsed);
    tokenizer.nextToken();
    return expressionParser.parse(tokenizer);
  }

  void parseStatement(ExpressionParser.Tokenizer tokenizer, List<Node> result, FunctionImplementation parsingContext) {
    String name = tokenizer.currentValue;

    switch (name.toUpperCase()) {
      case "CONST":
        result.add(parseDeclaration(tokenizer, DeclarationStatement.Kind.CONST));
        return;
      case "DEBUGGER":
        tokenizer.consumeIdentifier();
        result.add(new DebuggerStatement());
        return;
      case "DEF":
        result.add(parseDef(tokenizer));
        return;
      case "DIM":
        parseDim(tokenizer, result);
        return;
      case "ELSE":
        tokenizer.nextToken();
        result.add(new ElseStatement(result.size() == 0));
        return;
      case "END":
        tokenizer.consumeIdentifier();
        if (tokenizer.currentValue.equalsIgnoreCase("IF")) {
          tokenizer.consumeIdentifier();
          result.add(new EndIfStatement());
          return;
        }
        result.add(new EndStatement(/* invisible: */ false));
        return;
      case "ENDIF":
        tokenizer.consumeIdentifier();
        result.add(new EndIfStatement());
        return;
      case "FOR":
        result.add(parseFor(tokenizer));
        return;
      case "IF":
        parseIf(tokenizer, result);
        return;
      case "INPUT":
        result.add(parseIo(IoStatement.Kind.INPUT, tokenizer));
        return;
      case "LET":
        result.add(parseDeclaration(tokenizer, DeclarationStatement.Kind.LET));
        return;
      case "NEXT":
        parseNext(tokenizer, result);
        return;
      case "ON":
        result.add(parseOn(tokenizer));
        return;
      case "PRINT":
        result.add(parseIo(IoStatement.Kind.PRINT, tokenizer));
        return;
      case "REM":
        result.add(parseRem(tokenizer));
        return;
      case "READ":
        tokenizer.consumeIdentifier();
        ArrayList<Node> expressions = new ArrayList<>();
        do {
          expressions.add(expressionParser.parse(tokenizer));
        } while (tokenizer.tryConsume(","));
        result.add(new ReadStatement(expressions.toArray(Node.EMPTY_ARRAY)));
        return;

      case "RETURN":
        if (parsingContext != null && parsingContext != program.main) {
          result.add(parseFunctionReturn(tokenizer));
          return;
        }
        break;
    }
    for (Command.Kind kind : Command.Kind.values()) {
      if (name.equalsIgnoreCase(kind.name())) {
        if (parsingContext != null) {
            throw tokenizer.exception("Interactive command '" + name + "' can't be used in programs.", null);
        }
        result.add(parseCommand(tokenizer, kind));
        return;
      }
    }

    if (tryConsume(tokenizer, "GO")) {  // GO TO, GO SUB -> GOTO, GOSUB
      name += tokenizer.currentValue;
    } else if (name.equals("?")) {
      name = "PRINT";
    }

    if (name.equalsIgnoreCase("GOTO")) {
      tokenizer.nextToken();
      result.add(new GotoStatement(expressionParser.parse(tokenizer)));
      return;
    }

    for (LegacyStatement.Kind kind : LegacyStatement.Kind.values()) {
      if (name.equalsIgnoreCase(kind.name())) {
        result.add(parseLegacyStatement(tokenizer, kind));
        return;
      }
    }

    Node expression = expressionParser.parse(tokenizer);
    if ((expression instanceof RelationalOperator) && (expression.children[0] instanceof AssignableNode)
             && ((RelationalOperator) expression).getName().equals("=")) {
      try {
        result.add(new AssignStatement(expression.children[0], expression.children[1]));
      } catch (Exception e) {
        throw tokenizer.exception(null, e);
      }
    } else if (!tokenizer.currentValue.equals(":") && !tokenizer.currentValue.equals("")) {
      List<Node> params = new ArrayList<>();
      if (tokenizer.tryConsume(",")) {
        if (expression instanceof MathOperator && ((MathOperator) expression).kind == MathOperator.Kind.SUB) {
          params.add(expression.children[0]);
          params.add(new Group(new NegOperator(expression.children[1])));
        } else if (expression instanceof Apply  && expression.children.length == 2) {
          params.add(expression.children[0]);
          params.add(new Group(expression.children[1]));
        } else {
          throw tokenizer.exception("Unexpected comma", null);
        }
      } else {
        params.add(expression);
      }
      do {
        params.add(expressionParser.parse(tokenizer));
      } while (tokenizer.tryConsume(","));

      result.add(new Apply(false, params.toArray(Node.EMPTY_ARRAY)));
    } else if (expression instanceof Path || expression instanceof Identifier){
      result.add(new VoidStatement(expression));
    } else {
      result.add(expression);
    }
  }

  LegacyStatement parseLegacyStatement(ExpressionParser.Tokenizer tokenizer, LegacyStatement.Kind kind) {
    tokenizer.nextToken();
    switch (kind) {
      case RESTORE: // 0 or 1 param; Default is 0
        if (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF &&
            !tokenizer.currentValue.equals(":")) {
          return new LegacyStatement(kind, expressionParser.parse(tokenizer));
        }
        return new LegacyStatement(kind);

      case GOSUB:
        return new LegacyStatement(kind, expressionParser.parse(tokenizer));


      case DATA: { // One or more params
        ArrayList<Node> expressions = new ArrayList<>();
        do {
          expressions.add(expressionParser.parse(tokenizer));
        } while (tokenizer.tryConsume(","));
        return new LegacyStatement(kind, expressions.toArray(new Node[expressions.size()]));
      }
      default:
        return new LegacyStatement(kind);
    }
  }

  Command parseCommand(ExpressionParser.Tokenizer tokenizer, Command.Kind kind) {
    tokenizer.nextToken();
    switch (kind) {
      case EDIT:
      case RUN:  // 0 or 1 param; Default is 0
      case SAVE:
        if (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF &&
                !tokenizer.currentValue.equals(":")) {
          return new Command(kind, expressionParser.parse(tokenizer));
        }
        return new Command(kind);

      case DELETE:
      case LOAD: // Exactly one param
        return new Command(kind, expressionParser.parse(tokenizer));

      default:
        return new Command(kind);
    }
  }

  private void parseDim(ExpressionParser.Tokenizer tokenizer, List<Node> result) {
    tokenizer.nextToken();
    do {
      Node dimExpr = expressionParser.parse(tokenizer);
      if (!(dimExpr instanceof Apply)) {
        throw tokenizer.exception("DIM: Apply expected, got: " + dimExpr, null);
      }
      if (!(dimExpr.children[0] instanceof Identifier)) {
        throw tokenizer.exception("DIM: Identifier expected, got: " + dimExpr.children[0], null);
      }
      String varName = ((Identifier) dimExpr.children[0]).getName();
      if (dimExpr.children.length < 2) {
        throw tokenizer.exception("DIM: At least one dimension expected", null);
      }
      Node[] dimensions = new Node[dimExpr.children.length - 1];
      System.arraycopy(dimExpr.children, 1, dimensions, 0, dimensions.length);

      Type elementType;
      if (tokenizer.tryConsume("as") || tokenizer.tryConsume("AS") || tokenizer.tryConsume("As")) {
        elementType = parseType(tokenizer);
      } else {
        elementType = varName.endsWith("$") ? Types.STRING : Types.NUMBER;
      }
      result.add(new DimStatement(elementType, varName, dimensions));
    } while (tokenizer.tryConsume(","));
  }

  private void parseIf(ExpressionParser.Tokenizer tokenizer, List<Node> result) {
    boolean elseIf = (result.size() > 0 && result.get(result.size() - 1) instanceof ElseStatement);

    tokenizer.nextToken();
    Node condition = expressionParser.parse(tokenizer);
    if (!tryConsume(tokenizer, "THEN") && !tryConsume(tokenizer, "GOTO")) {
      throw tokenizer.exception("'THEN expected after IF-condition.'", null);
    }
    result.add(new IfStatement(condition, tokenizer.currentValue.isEmpty(), elseIf));
    if (tokenizer.currentType == ExpressionParser.Tokenizer.TokenType.NUMBER) {
      double target = (int) Double.parseDouble(tokenizer.currentValue);
      tokenizer.nextToken();
      result.add(new GotoStatement(new Literal(target)));
    }
  }

  private Node parseOn(ExpressionParser.Tokenizer tokenizer) {
    tokenizer.nextToken();
    Node expr = expressionParser.parse(tokenizer);
    String[] suffix = new String[1];
    if (tryConsume(tokenizer, "GOTO")) {
      suffix[0] = " GOTO ";
    } else if (tryConsume(tokenizer, "GOSUB")) {
      suffix[0] = " GOSUB ";
    } else {
      return new OnStatement(expr);
    }
    List<Node> expressions = new ArrayList<Node>();
    expressions.add(expr);
    do {
      expressions.add(expressionParser.parse(tokenizer));
    } while (tokenizer.tryConsume(","));
    return new LegacyStatement(LegacyStatement.Kind.ON, suffix,
            expressions.toArray(new Node[expressions.size()]));
  }


  private IoStatement parseIo(IoStatement.Kind kind, ExpressionParser.Tokenizer tokenizer) {
    tokenizer.nextToken();
    List<Node> args = new ArrayList<>();
    List<String> delimiter = new ArrayList<>();
    while (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF
            && !tokenizer.currentValue.equals(":")) {
      if (tokenizer.currentValue.equals(",") || tokenizer.currentValue.equals(";")) {
        delimiter.add(tokenizer.currentValue + " ");
        tokenizer.nextToken();
        if (delimiter.size() > args.size()) {
          args.add(new Literal(Program.INVISIBLE_STRING));
        }
      } else {
        args.add(expressionParser.parse(tokenizer));
      }
    }
    return new IoStatement(kind, delimiter.toArray(new String[delimiter.size()]),
            args.toArray(new Node[args.size()]));
  }

  private Node parseFor(ExpressionParser.Tokenizer tokenizer) {
    tokenizer.nextToken();
    Node assignment = expressionParser.parse(tokenizer);
    if (!(assignment instanceof RelationalOperator) || !(assignment.children[0] instanceof Identifier)
            || assignment.children[0].children.length != 0
            || !((RelationalOperator) assignment).getName().equals("=")) {
      tokenizer.exception("LocalVariable assignment expected after FOR", null);
    }
    String varName = ((Identifier) assignment.children[0]).getName();
    require(tokenizer, "TO");
    Node end = expressionParser.parse(tokenizer);
    if (tryConsume(tokenizer, "STEP")) {
      return new ForStatement(varName, assignment.children[1], end,
              expressionParser.parse(tokenizer));
    }
    return new ForStatement(varName, assignment.children[1], end);
  }

  private DefStatement parseDef(ExpressionParser.Tokenizer tokenizer) {
    tokenizer.consumeIdentifier();  // def

    String name = tokenizer.consumeIdentifier();

    tokenizer.consume("(");
    ArrayList<String> parameterNames = new ArrayList<>();
    if (!tokenizer.tryConsume(")")) {
      do {
        parameterNames.add(tokenizer.consumeIdentifier());
      }while (tokenizer.tryConsume(","));
      tokenizer.consume(")");
    }

    tokenizer.consume("=");
    Node body = expressionParser.parse(tokenizer);

    Type[] parameterTypes = new Type[parameterNames.size()];
    for (int i = 0; i < parameterTypes.length; i++) {
      parameterTypes[i] = parameterNames.get(i).endsWith("$") ? Types.STRING : Types.NUMBER;
    }
    FunctionImplementation fn = new FunctionImplementation(program, new FunctionTypeImpl(name.endsWith("$") ? Types.STRING : Types.NUMBER, parameterTypes), parameterNames.toArray(new String[0]));
    fn.setLine(new CodeLine(10, new FunctionReturnStatement(body)));

    return new DefStatement(name, fn);
  }

  private Node parseDeclaration(ExpressionParser.Tokenizer tokenizer, DeclarationStatement.Kind kind) {
    tokenizer.nextToken();
    Node assignment = expressionParser.parse(tokenizer);
    if (!(assignment instanceof RelationalOperator) || !(assignment.children[0] instanceof AssignableNode)
            || !((RelationalOperator) assignment).getName().equals("=")) {
      throw tokenizer.exception("Unrecognized statement or illegal assignment: '"
              + assignment + "'.", null);
    }
    if (assignment.children[0] instanceof Identifier) {
      String varName = ((Identifier) assignment.children[0]).getName();
      return new DeclarationStatement(kind, varName, assignment.children[1]);
    }
    if (kind == DeclarationStatement.Kind.CONST) {
      throw tokenizer.exception("Left hand side of CONST assignment must be a variable.", null);
    }
    try {
      return new AssignStatement(assignment.children[0], assignment.children[1]);
    } catch (Exception e) {
      throw tokenizer.exception("Error parsing " + kind, e);
    }
  }

  private void parseNext(ExpressionParser.Tokenizer tokenizer, List<Node> result) {
    tokenizer.nextToken();
    if (tokenizer.currentType == ExpressionParser.Tokenizer.TokenType.IDENTIFIER) {
      do {
        result.add(new NextStatement(tokenizer.consumeIdentifier()));
      } while (tokenizer.tryConsume(","));
    } else if (program.isLegacyMode()){
      result.add(new NextStatement(null));
    } else {
      tokenizer.exception("Variable name expected after NEXT.", null);
    }
  }

  private RemStatement parseRem(ExpressionParser.Tokenizer tokenizer) {
    tokenizer.nextToken();
    StringBuilder sb = new StringBuilder();
    while (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF) {
      sb.append(tokenizer.leadingWhitespace).append(tokenizer.currentValue);
      tokenizer.nextToken();
    }
    if (sb.length() > 0 && sb.charAt(0) == ' ') {
      sb.deleteCharAt(0);
    }
    return new RemStatement(sb.toString());
  }

  private FunctionReturnStatement parseFunctionReturn(ExpressionParser.Tokenizer tokenizer) {
    tokenizer.nextToken();
    if (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF &&
            !tokenizer.currentValue.equals(":")) {
      return new FunctionReturnStatement(expressionParser.parse(tokenizer));
    }
    return new FunctionReturnStatement();
  }


  public AbstractDeclarationStatement parseDeclaration(ExpressionParser.Tokenizer tokenizer, boolean permitUninitialized) {
    AbstractDeclarationStatement result;
    if (tokenizer.currentValue.equalsIgnoreCase("let")
        || tokenizer.currentValue.equalsIgnoreCase("dim")
        || tokenizer.currentValue.equalsIgnoreCase("const")) {
      ArrayList<Node> statements = new ArrayList<>();
      parseStatement(tokenizer, statements, null);
      result = (AbstractDeclarationStatement) statements.get(0);
    } else if (!permitUninitialized) {
      throw tokenizer.exception("LET, DIM or CONST expected.", null);
    } else {
      Type type = parseType(tokenizer); // consumeType
      String fieldName = tokenizer.consumeIdentifier();
      result = new UninitializedField(type, fieldName);
    }
    if (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF) {
      throw tokenizer.exception("Leftover input", null);
    }
    return result;
  }

  public List<? extends Node> parseStatementList(ExpressionParser.Tokenizer tokenizer, FunctionImplementation parsingContext) {
    ArrayList<Node> result = new ArrayList<>();
    Node statement;
    do {
      while (tokenizer.tryConsume(":")) {
        result.add(new LegacyStatement(null));
      }
      if (tokenizer.currentType == ExpressionParser.Tokenizer.TokenType.EOF) {
        break;
      }
      parseStatement(tokenizer, result, parsingContext);
      statement = result.get(result.size() - 1);
    } while (statement instanceof IfStatement
            || statement instanceof ElseStatement
            || tokenizer.currentValue.equalsIgnoreCase("else")
            || tokenizer.tryConsume(":"));
    if (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF) {
      throw tokenizer.exception("Leftover input.", null);
    }
    return result;
  }

  void require(ExpressionParser.Tokenizer tokenizer, String s) {
    if (!tryConsume(tokenizer, s)) {
      throw tokenizer.exception("Expected: '" + s + "'.", null);
    }
  }

  boolean tryConsume(ExpressionParser.Tokenizer tokenizer, String s) {
    if (tokenizer.currentValue.equalsIgnoreCase(s)) {
      tokenizer.nextToken();
      return true;
    }
    return false;
  }


  public Type parseType(ExpressionParser.Tokenizer tokenizer) {
     String typeName = tokenizer.consumeIdentifier();
     Type result;
     if (typeName.equalsIgnoreCase("Number")) {
         result = Types.NUMBER;
     } else if (typeName.equalsIgnoreCase("String")) {
         result = Types.STRING;
     } else {
       GlobalSymbol symbol = program.getSymbol(typeName);
       if (symbol == null) {
         throw new RuntimeException("Unrecognized type: " + typeName);
       }
       if (!(symbol.getValue() instanceof Type)) {
         throw new RuntimeException("'" + typeName + "' is not a type!");
       }
       result = (Type) symbol.getValue();
     }
     while (tokenizer.tryConsume("[")) {
       tokenizer.consume("]");
       result = new ArrayType(result);
     }

     return  result;
 }
}
