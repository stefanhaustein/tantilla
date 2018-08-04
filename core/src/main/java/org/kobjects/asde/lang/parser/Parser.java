package org.kobjects.asde.lang.parser;

import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.node.AssignableNode;
import org.kobjects.asde.lang.node.Literal;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.node.Operator;
import org.kobjects.asde.lang.node.Statement;
import org.kobjects.asde.lang.node.Variable;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.expressionparser.ExpressionParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
  final Program program;
  final ExpressionParser<Node> expressionParser;

  static char returnTypeCode(Type type) {
    if (type == Type.NUMBER) {
      return 'D';
    }
    if (type == Type.STRING) {
      return 'S';
    }
    return 'O';
  }

  public Parser(Program program) {
    this.program = program;

    expressionParser = new ExpressionParser<>(new ExpressionBuilder(program));
    expressionParser.addCallBrackets("(", ",", ")");
    expressionParser.addCallBrackets("[", ",", "]");  // HP
    expressionParser.addGroupBrackets("(", null, ")");
    expressionParser.addOperators(ExpressionParser.OperatorType.INFIX, 9, ".");
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
    return new GwTokenizer(new Scanner(line), expressionParser.getSymbols());
  }

  Statement parseStatement(ExpressionParser.Tokenizer tokenizer) {
    String name = tokenizer.currentValue;
    if (tryConsume(tokenizer, "GO")) {  // GO TO, GO SUB -> GOTO, GOSUB
      name += tokenizer.currentValue;
    } else if (name.equals("?")) {
      name = "PRINT";
    }
    Statement.Kind type = null;
    for (Statement.Kind t : Statement.Kind.values()) {
      if (name.equalsIgnoreCase(t.name())) {
        type = t;
        break;
      }
    }
    if (type == null) {
      Node expression = expressionParser.parse(tokenizer);

      if ((expression instanceof Operator) && (expression.children[0] instanceof AssignableNode)
              && ((Operator) expression).name.equals("=")) {
        return new Statement(program, Statement.Kind.LET, new String[]{" = "}, expression.children);
      }
      return new Statement(program, Statement.Kind.PRINT, new String[0], expression);
    }
    tokenizer.nextToken();

    switch (type) {
      case RUN:  // 0 or 1 param; Default is 0
      case RESTORE:
        if (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF &&
            !tokenizer.currentValue.equals(":")) {
          return new Statement(program, type, expressionParser.parse(tokenizer));
        }
        return new Statement(program, type);

      case DEF:  // Exactly one param
      case GOTO:
      case GOSUB:
      case LOAD:
        return new Statement(program, type, expressionParser.parse(tokenizer));

      case NEXT:   // Zero of more
        ArrayList<Node> vars = new ArrayList<>();
        if (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF &&
            !tokenizer.currentValue.equals(":")) {
          do {
            vars.add(expressionParser.parse(tokenizer));
          } while (tokenizer.tryConsume(","));
        }
        return new Statement(program, type, vars.toArray(new Node[vars.size()]));

      case DATA:  // One or more params
      case DIM:
      case READ: {
        ArrayList<Node> expressions = new ArrayList<>();
        do {
          expressions.add(expressionParser.parse(tokenizer));
        } while (tokenizer.tryConsume(","));
        return new Statement(program, type, expressions.toArray(new Node[expressions.size()]));
      }

      case FOR: {
        Node assignment = expressionParser.parse(tokenizer);
        if (!(assignment instanceof Operator) || !(assignment.children[0] instanceof Variable)
            || assignment.children[0].children.length != 0
            || !((Operator) assignment).name.equals("=")) {
          throw new RuntimeException("LocalVariable assignment expected after FOR");
        }
        require(tokenizer, "TO");
        Node end = expressionParser.parse(tokenizer);
        if (tryConsume(tokenizer, "STEP")) {
          return new Statement(program, type, new String[]{" = ", " TO ", " STEP "},
              assignment.children[0], assignment.children[1], end,
              expressionParser.parse(tokenizer));
        }
        return new Statement(program, type, new String[]{" = ", " TO "},
            assignment.children[0], assignment.children[1], end);
      }

      case IF:
        Node condition = expressionParser.parse(tokenizer);
        if (!tryConsume(tokenizer, "THEN") && !tryConsume(tokenizer, "GOTO")) {
          throw tokenizer.exception("'THEN expected after IF-condition.'", null);
        }
        if (tokenizer.currentType == ExpressionParser.Tokenizer.TokenType.NUMBER) {
          double target = (int) Double.parseDouble(tokenizer.currentValue);
          tokenizer.nextToken();
          return new Statement(program, type, new String[]{" THEN "}, condition,
              new Literal(target));
        }
        return new Statement(program, type, new String[]{" THEN"}, condition);

      case INPUT:
      case PRINT:
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
        return new Statement(program, type, delimiter.toArray(new String[delimiter.size()]),
            args.toArray(new Node[args.size()]));

      case LET: {
        Node assignment = expressionParser.parse(tokenizer);
        if (!(assignment instanceof Operator) || !(assignment.children[0] instanceof AssignableNode)
            || !((Operator) assignment).name.equals("=")) {
          throw tokenizer.exception("Unrecognized statement or illegal assignment: '"
              + assignment + "'.", null);
        }
        return new Statement(program, type, new String[]{" = "}, assignment.children);
      }
      case ON: {
        List<Node> expressions = new ArrayList<Node>();
        expressions.add(expressionParser.parse(tokenizer));
        String[] kind = new String[1];
        if (tryConsume(tokenizer, "GOTO")) {
          kind[0] = " GOTO ";
        } else if (tryConsume(tokenizer, "GOSUB")) {
          kind[0] = " GOSUB ";
        } else {
          throw tokenizer.exception("GOTO or GOSUB expected.", null);
        }
        do {
          expressions.add(expressionParser.parse(tokenizer));
        } while (tokenizer.tryConsume(","));
        return new Statement(program, type, kind,
            expressions.toArray(new Node[expressions.size()]));
      }
      case REM: {
        StringBuilder sb = new StringBuilder();
        while (tokenizer.currentType != ExpressionParser.Tokenizer.TokenType.EOF) {
          sb.append(tokenizer.leadingWhitespace).append(tokenizer.currentValue);
          tokenizer.nextToken();
        }
        if (sb.length() > 0 && sb.charAt(0) == ' ') {
          sb.deleteCharAt(0);
        }
        return new Statement(program, type, new Variable(program, sb.toString()));
      }
      default:
        return new Statement(program, type);
    }
  }

  public List<Statement> parseStatementList(ExpressionParser.Tokenizer tokenizer) {
    ArrayList<Statement> result = new ArrayList<>();
    Statement statement;
    do {
      while (tokenizer.tryConsume(":")) {
        result.add(new Statement(program, null));
      }
      if (tokenizer.currentType == ExpressionParser.Tokenizer.TokenType.EOF) {
        break;
      }
      statement = parseStatement(tokenizer);
      result.add(statement);
    } while (statement.kind == Statement.Kind.IF ? statement.children.length == 1
        : tokenizer.tryConsume(":"));
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

  /**
   * A tokenizer subclass that splits identifiers if they contain reserved words,
   * so it will report "IFA<4THENPRINTZ" as "IF" "A" "<" "4" "THEN" "PRINT" "Z"
   */
  static class GwTokenizer extends ExpressionParser.Tokenizer {
    static Pattern reservedWordPattern;
    static {
      StringBuilder sb = new StringBuilder();
      for (Statement.Kind t: Statement.Kind.values()) {
        sb.append(t.name());
        sb.append('|');
      }
      sb.append("AND|ELSE|NOT|OR|STEP|TO|THEN");
      reservedWordPattern = Pattern.compile(sb.toString());
    }

    Matcher gwMatcher;
    String gwIdentifier;
    int gwConsumed = 0;

    GwTokenizer(Scanner scanner, Iterable<String> symbols) {
      super(scanner, symbols, ":", ";", "?");
      stringPattern = Pattern.compile("\\G\\s*(\"[^\"]*\")+");
    }

    private TokenType gwToken(int start, int end) {
      currentValue = gwIdentifier.substring(start, end);
      if (end == gwIdentifier.length()) {
        gwIdentifier = null;
        gwMatcher = null;
        gwConsumed = 0;
      } else {
        gwConsumed = end;
      }
      currentType = currentValue.matches("\\d+") ? TokenType.NUMBER : TokenType.IDENTIFIER;
      return currentType;
    }

    public TokenType nextToken() {
      if (gwIdentifier != null && gwConsumed < gwIdentifier.length()) {
        if (gwConsumed == gwMatcher.start()) {
          return gwToken(gwConsumed, gwMatcher.end());
        }
        if (gwMatcher.find()) {
          return gwToken(gwConsumed, gwMatcher.start() > gwConsumed
              ? gwMatcher.start() : gwMatcher.end());
        }
        return gwToken(gwConsumed, gwIdentifier.length());
      }

      super.nextToken();

      if (currentType == ExpressionParser.Tokenizer.TokenType.IDENTIFIER) {
        gwMatcher = reservedWordPattern.matcher(currentValue);
        if (gwMatcher.find()) {
          gwIdentifier = currentValue;
          return gwToken(0, gwMatcher.start() == 0 ? gwMatcher.end() : gwMatcher.start());
        }
        gwMatcher = null;
      }
      return currentType;
    }
  }


}
