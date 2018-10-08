package org.kobjects.asde.lang.parser;

import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.node.Apply;
import org.kobjects.asde.lang.node.Group;
import org.kobjects.asde.lang.node.Path;
import org.kobjects.asde.lang.statement.AssignStatement;
import org.kobjects.asde.lang.node.AssignableNode;
import org.kobjects.asde.lang.statement.Command;
import org.kobjects.asde.lang.statement.DimStatement;
import org.kobjects.asde.lang.statement.ForStatement;
import org.kobjects.asde.lang.statement.IfStatement;
import org.kobjects.asde.lang.statement.IoStatement;
import org.kobjects.asde.lang.statement.LetStatement;
import org.kobjects.asde.lang.node.Literal;
import org.kobjects.asde.lang.statement.NextStatement;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.node.Operator;
import org.kobjects.asde.lang.statement.RemStatement;
import org.kobjects.asde.lang.statement.ReturnStatement;
import org.kobjects.asde.lang.statement.LegacyStatement;
import org.kobjects.asde.lang.node.Identifier;
import org.kobjects.asde.lang.statement.VoidStatement;
import org.kobjects.expressionparser.ExpressionParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Parser {
  final ExpressionParser<Node> expressionParser;
  final Program program;

  public Parser(Program program) {
    this.program = program;

    expressionParser = new ExpressionParser<>(new ExpressionBuilder(program));
    expressionParser.addApplyBrackets(9,"(", ",", ")");
    expressionParser.addApplyBrackets(9,"[", ",", "]");  // HP
    expressionParser.addGroupBrackets("(", null, ")");
    expressionParser.addOperators(ExpressionParser.OperatorType.INFIX, 10, ".");
    expressionParser.addOperators(ExpressionParser.OperatorType.INFIX, 8, "^");
    expressionParser.addOperators(ExpressionParser.OperatorType.PREFIX, 7, "-");
    expressionParser.addOperators(ExpressionParser.OperatorType.INFIX, 6, "*", "/");
    expressionParser.addOperators(ExpressionParser.OperatorType.INFIX, 5, "+", "-");
    expressionParser.addOperators(ExpressionParser.OperatorType.INFIX, 4, ">=", "<=", "<>", ">", "<", "=");
    expressionParser.addOperators(ExpressionParser.OperatorType.PREFIX, 3, "not", "NOT", "Not");
    expressionParser.addOperators(ExpressionParser.OperatorType.INFIX, 2, "and", "AND", "And");
    expressionParser.addOperators(ExpressionParser.OperatorType.INFIX, 1, "or", "OR", "Or");
  }


  public ExpressionParser.Tokenizer createTokenizer(String line) {
    return new ExpressionParser.Tokenizer(new Scanner(line), expressionParser.getSymbols(), "->");
  }


  void parseStatement(ExpressionParser.Tokenizer tokenizer, List<Node> result) {
    String name = tokenizer.currentValue;

    switch (name.toUpperCase()) {
      case "DIM":
        parseDim(tokenizer, result);
        return;
      case "FOR":
        result.add(parseFor(tokenizer));
        return;
      case "INPUT":
        result.add(parseIo(IoStatement.Kind.INPUT, tokenizer));
        return;
      case "LET":
        result.add(parseLet(tokenizer));
        return;
      case "IF":
        parseIf(tokenizer, result);
        return;
      case "NEXT":
        parseNext(tokenizer, result);
        return;
      case "PRINT":
        result.add(parseIo(IoStatement.Kind.PRINT, tokenizer));
        return;
      case "REM":
        result.add(parseRem(tokenizer));
        return;
      case "RETURN":
        result.add(parseReturn(tokenizer));
        return;
    }
    for (Command.Kind kind : Command.Kind.values()) {
      if (name.equalsIgnoreCase(kind.name())) {
        result.add(parseCommand(tokenizer, kind));
        return;
      }
    }

    if (tryConsume(tokenizer, "GO")) {  // GO TO, GO SUB -> GOTO, GOSUB
      name += tokenizer.currentValue;
    } else if (name.equals("?")) {
      name = "PRINT";
    }

    for (LegacyStatement.Kind kind : LegacyStatement.Kind.values()) {
      if (name.equalsIgnoreCase(kind.name())) {
        result.add(parseStatement(tokenizer, kind));
        return;
      }
    }

    Node expression = expressionParser.parse(tokenizer);
    if ((expression instanceof Operator) && (expression.children[0] instanceof AssignableNode)
             && ((Operator) expression).name.equals("=")) {
      result.add(new AssignStatement(expression.children[0], expression.children[1]));
    } else if (!tokenizer.currentValue.equals(":") && !tokenizer.currentValue.equals("")) {
      List<Node> params = new ArrayList<>();
      if (tokenizer.tryConsume(",")) {
        if (expression instanceof Operator && ((Operator) expression).name.equals("-")) {
          params.add(expression.children[0]);
          params.add(new Group(new Operator("-", expression.children[1])));
        } else if (expression instanceof Apply  && expression.children.length == 2) {
          params.add(expression.children[0]);
          params.add(new Group(expression.children[1]));
        } else {
          throw new RuntimeException("Unexpected comma");
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

  LegacyStatement parseStatement(ExpressionParser.Tokenizer tokenizer, LegacyStatement.Kind kind) {
    tokenizer.nextToken();
    switch (kind) {
      case RESTORE: // 0 or 1 param; Default is 0
        if (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF &&
            !tokenizer.currentValue.equals(":")) {
          return new LegacyStatement(program, kind, expressionParser.parse(tokenizer));
        }
        return new LegacyStatement(program, kind);

      case DEF:  // Exactly one param
      case GOTO:
      case GOSUB:
      case PAUSE:
        return new LegacyStatement(program, kind, expressionParser.parse(tokenizer));


      case DATA:  // One or more params
      case READ: {
        ArrayList<Node> expressions = new ArrayList<>();
        do {
          expressions.add(expressionParser.parse(tokenizer));
        } while (tokenizer.tryConsume(","));
        return new LegacyStatement(program, kind, expressions.toArray(new Node[expressions.size()]));
      }

      case ON: {
        List<Node> expressions = new ArrayList<Node>();
        expressions.add(expressionParser.parse(tokenizer));
        String[] suffix = new String[1];
        if (tryConsume(tokenizer, "GOTO")) {
          suffix[0] = " GOTO ";
        } else if (tryConsume(tokenizer, "GOSUB")) {
          suffix[0] = " GOSUB ";
        } else {
          throw tokenizer.exception("GOTO or GOSUB expected.", null);
        }
        do {
          expressions.add(expressionParser.parse(tokenizer));
        } while (tokenizer.tryConsume(","));
        return new LegacyStatement(program, kind, suffix,
            expressions.toArray(new Node[expressions.size()]));
      }
      default:
        return new LegacyStatement(program, kind);
    }
  }

  Command parseCommand(ExpressionParser.Tokenizer tokenizer, Command.Kind kind) {
    tokenizer.nextToken();
    switch (kind) {
      case RUN:  // 0 or 1 param; Default is 0
      case SAVE:
        if (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF &&
                !tokenizer.currentValue.equals(":")) {
          return new Command(kind, expressionParser.parse(tokenizer));
        }
        return new Command(kind);

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
        throw new RuntimeException("DIM: Apply expected, got: " + dimExpr);
      }
      if (!(dimExpr.children[0] instanceof Identifier)) {
        throw new RuntimeException("DIM: Identifier expected, got: " + dimExpr.children[0]);
      }
      if (dimExpr.children.length < 2) {
        throw new RuntimeException("DIM: At least one dimension expected");
      }
      Node[] dimensions = new Node[dimExpr.children.length - 1];
      System.arraycopy(dimExpr.children, 1, dimensions, 0, dimensions.length);
      result.add(new DimStatement(((Identifier) dimExpr.children[0]).name, dimensions));
    } while (tokenizer.tryConsume(","));
  }

  private void parseIf(ExpressionParser.Tokenizer tokenizer, List<Node> result) {
    tokenizer.nextToken();
    result.add(new IfStatement(expressionParser.parse(tokenizer)));
    if (!tryConsume(tokenizer, "THEN") && !tryConsume(tokenizer, "GOTO")) {
      throw tokenizer.exception("'THEN expected after IF-condition.'", null);
    }
    if (tokenizer.currentType == ExpressionParser.Tokenizer.TokenType.NUMBER) {
      double target = (int) Double.parseDouble(tokenizer.currentValue);
      tokenizer.nextToken();
      result.add(new LegacyStatement(program, LegacyStatement.Kind.GOTO, new Literal(target)));
    }
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
    if (!(assignment instanceof Operator) || !(assignment.children[0] instanceof Identifier)
            || assignment.children[0].children.length != 0
            || !((Operator) assignment).name.equals("=")) {
      throw new RuntimeException("LocalVariable assignment expected after FOR");
    }
    String varName = ((Identifier) assignment.children[0]).name;
    require(tokenizer, "TO");
    Node end = expressionParser.parse(tokenizer);
    if (tryConsume(tokenizer, "STEP")) {
      return new ForStatement(varName, assignment.children[1], end,
              expressionParser.parse(tokenizer));
    }
    return new ForStatement(varName, assignment.children[1], end);
  }

  private Node parseLet(ExpressionParser.Tokenizer tokenizer) {
    tokenizer.nextToken();
    Node assignment = expressionParser.parse(tokenizer);
    if (!(assignment instanceof Operator) || !(assignment.children[0] instanceof AssignableNode)
            || !((Operator) assignment).name.equals("=")) {
      throw tokenizer.exception("Unrecognized statement or illegal assignment: '"
              + assignment + "'.", null);
    }
    if (assignment.children[0] instanceof Identifier) {
      String varName = ((Identifier) assignment.children[0]).name;
      return new LetStatement(varName, assignment.children[1]);
    }
    return new AssignStatement(assignment.children[0], assignment.children[1]);
  }

  private void parseNext(ExpressionParser.Tokenizer tokenizer, List<Node> result) {
    tokenizer.nextToken();
    if (tokenizer.currentType == ExpressionParser.Tokenizer.TokenType.IDENTIFIER) {
      do {
        result.add(new NextStatement(tokenizer.consumeIdentifier()));
      } while (tokenizer.tryConsume(","));
    } else {
      result.add(new NextStatement(null));
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

  private ReturnStatement parseReturn(ExpressionParser.Tokenizer tokenizer) {
    tokenizer.nextToken();
    if (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF &&
            !tokenizer.currentValue.equals(":")) {
      return new ReturnStatement(expressionParser.parse(tokenizer));
    }
    return new ReturnStatement();
  }

  public List<? extends Node> parseStatementList(ExpressionParser.Tokenizer tokenizer) {
    ArrayList<Node> result = new ArrayList<>();
    Node statement;
    do {
      while (tokenizer.tryConsume(":")) {
        result.add(new LegacyStatement(program, null));
      }
      if (tokenizer.currentType == ExpressionParser.Tokenizer.TokenType.EOF) {
        break;
      }
      parseStatement(tokenizer, result);
      statement = result.get(result.size() - 1);
    } while (statement instanceof IfStatement || tokenizer.tryConsume(":"));
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


}
