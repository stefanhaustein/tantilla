package org.kobjects.asde.lang.parser;

import org.kobjects.asde.lang.list.ListType;
import org.kobjects.asde.lang.function.FunctionImplementation;
import org.kobjects.asde.lang.node.InvokeMethod;
import org.kobjects.asde.lang.node.Path;
import org.kobjects.asde.lang.program.GlobalSymbol;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.node.Apply;
import org.kobjects.asde.lang.node.Group;
import org.kobjects.asde.lang.node.NegOperator;
import org.kobjects.asde.lang.statement.AbstractDeclarationStatement;
import org.kobjects.asde.lang.statement.AssignStatement;
import org.kobjects.asde.lang.statement.BlockStatement;
import org.kobjects.asde.lang.statement.Command;
import org.kobjects.asde.lang.statement.ConditionStatement;
import org.kobjects.asde.lang.statement.DebuggerStatement;
import org.kobjects.asde.lang.statement.EndStatement;
import org.kobjects.asde.lang.statement.ForStatement;
import org.kobjects.asde.lang.statement.OnChangeStatement;
import org.kobjects.asde.lang.statement.PrintStatement;
import org.kobjects.asde.lang.statement.DeclarationStatement;
import org.kobjects.asde.lang.node.Literal;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.node.MathOperator;
import org.kobjects.asde.lang.statement.OnStatement;
import org.kobjects.asde.lang.statement.RemStatement;
import org.kobjects.asde.lang.statement.ReturnStatement;
import org.kobjects.asde.lang.statement.Statement;
import org.kobjects.asde.lang.statement.UninitializedField;
import org.kobjects.asde.lang.statement.VoidStatement;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.statement.WhileStatement;
import org.kobjects.expressionparser.Tokenizer;
import org.kobjects.asde.lang.type.Type;

import java.util.ArrayList;
import java.util.List;

public class StatementParser {
  final AsdeExpressionParser expressionParser;
  final Program program;

  public StatementParser(Program program) {
    this.program = program;

    expressionParser = new AsdeExpressionParser(program);
  }

  public Tokenizer createTokenizer(String line) {
    return expressionParser.createTokenizer(line);
  }

  public Node parseExpression(String unparsed) {
    Tokenizer tokenizer = expressionParser.createTokenizer(unparsed);
    tokenizer.nextToken();
    return expressionParser.parse(tokenizer);
  }

  void parseStatement(Tokenizer tokenizer, List<Statement> result, FunctionImplementation parsingContext) {
    String name = tokenizer.currentValue;

    switch (name.toLowerCase()) {
      case "const":
      case "var":
        result.add(parseDeclaration(tokenizer, false));
        return;
      case "debugger":
        tokenizer.consumeIdentifier();
        result.add(new DebuggerStatement());
        return;
      case "else":
        tokenizer.nextToken();
        result.add(new ConditionStatement(ConditionStatement.Kind.ELSE, new Literal(Boolean.TRUE)));
        tokenizer.consume(":");
        return;
      case "end":
        tokenizer.consumeIdentifier();
        result.add(new EndStatement());
        return;
      case "elif":
        parseConditional(tokenizer, ConditionStatement.Kind.ELIF, result);
        return;
      case "for":
        result.add(parseFor(tokenizer));
        return;
      case "if":
        parseConditional(tokenizer, ConditionStatement.Kind.IF, result);
        return;
      case "on":
        result.add(parseOn(tokenizer));
        return;
      case "onchange":
        result.add(parseOnchange(tokenizer));
        return;
      case "print":
        result.add(parsePrint(tokenizer));
        return;
      case "rem":
        result.add(parseRem(tokenizer));
        return;

      case "while":
        tokenizer.nextToken();
        result.add(new WhileStatement(expressionParser.parse(tokenizer)));
        if (!tryConsume(tokenizer, ":")) {
          throw tokenizer.exception("':' expected after 'while'-condition.'", null);
        }
        return;

      case "return":
        result.add(parseFunctionReturn(tokenizer));
        return;
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

    Node expression = expressionParser.parse(tokenizer);
    if (tokenizer.tryConsume("=")) {
      try {
        result.add(new AssignStatement(expression, expressionParser.parse(tokenizer)));
      } catch (Exception e) {
        throw tokenizer.exception(null, e);
      }
    } else if (!tokenizer.currentValue.equals(";") && !tokenizer.currentValue.equals("")) {
      List<Node> params = new ArrayList<>();
      if (tokenizer.tryConsume(",")) {
        if (expression instanceof MathOperator && ((MathOperator) expression).kind == MathOperator.Kind.SUB) {
          params.add(expression.children[0]);
          params.add(new Group(new NegOperator(expression.children[1])));
        } else if ((expression instanceof Apply || expression instanceof InvokeMethod) && expression.children.length == 2) {
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

      if (expression instanceof InvokeMethod) {
        result.add(new VoidStatement(new InvokeMethod(((InvokeMethod) expression).name, params.toArray(Node.EMPTY_ARRAY))));
      } else if (params.get(0) instanceof Path) {
        Path path = (Path) params.get(0);
        params.set(0, path.children[0]);
        result.add(new VoidStatement(new InvokeMethod(path.pathName, params.toArray(Node.EMPTY_ARRAY))));
      } else {
        result.add(new VoidStatement(new Apply(false, params.toArray(Node.EMPTY_ARRAY))));
      }
    } else {//if (expression instanceof Path || expression instanceof Identifier){
      result.add(new VoidStatement(expression));
    } /*else {
      result.add(expression);
    }*/
  }


  Command parseCommand(Tokenizer tokenizer, Command.Kind kind) {
    tokenizer.nextToken();
    switch (kind) {
      case EDIT:
      case RUN:  // 0 or 1 param; Default is 0
      case SAVE:
        if (tokenizer.currentType != Tokenizer.TokenType.EOF &&
                !tokenizer.currentValue.equals(";")) {
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

  private void parseConditional(Tokenizer tokenizer, ConditionStatement.Kind kind, List<Statement> result) {
    tokenizer.nextToken();
    Node condition = expressionParser.parse(tokenizer);
    if (!tryConsume(tokenizer, ":")) {
      throw tokenizer.exception("':' expected after '" + kind.name().toLowerCase() + "'-condition.'", null);
    }
    result.add(new ConditionStatement(kind, condition));
  }

  private OnStatement parseOn(Tokenizer tokenizer) {
    tokenizer.nextToken();
    Node expr = expressionParser.parse(tokenizer);
    if (!tryConsume(tokenizer, ":")) {
      throw new RuntimeException("':' expected.");
    }
    return new OnStatement(expr);
  }

  private OnChangeStatement parseOnchange(Tokenizer tokenizer) {
    tokenizer.nextToken();
    Node expr = expressionParser.parse(tokenizer);
    if (!tryConsume(tokenizer, ":")) {
      throw new RuntimeException("':' expected.");
    }
    return new OnChangeStatement(expr);
  }


  private PrintStatement parsePrint(Tokenizer tokenizer) {
    tokenizer.nextToken();
    List<Node> args = new ArrayList<>();
    while (tokenizer.currentType != Tokenizer.TokenType.EOF
            && !tokenizer.currentValue.equals(";")) {
      args.add(expressionParser.parse(tokenizer));
      if (!tokenizer.tryConsume(",")) {
        break;
      }
    }
    return new PrintStatement(args.toArray(new Node[0]));
  }

  private ForStatement parseFor(Tokenizer tokenizer) {
    tokenizer.nextToken();
    String varName = tokenizer.consumeIdentifier();
    require(tokenizer, "in");
    Node iterable = expressionParser.parse(tokenizer);
    tokenizer.consume(":");
    return new ForStatement(varName, iterable);
  }

  AbstractDeclarationStatement parseDeclaration(Tokenizer tokenizer, boolean permitUninitialized) {
    DeclarationStatement.Kind kind;
    if (tokenizer.tryConsume("var")) {
      kind = DeclarationStatement.Kind.VAR;
    } else if (tokenizer.tryConsume("const")) {
      kind = DeclarationStatement.Kind.CONST;
    } else {
      throw new RuntimeException("var or const expected");
    }
    String varName = tokenizer.consumeIdentifier();
    if (tokenizer.tryConsume("=")) {
      Node value = expressionParser.parse(tokenizer);
      return new DeclarationStatement(kind, varName, value);
    } else if (tokenizer.tryConsume(":")) {
      if (!permitUninitialized) {
        throw new RuntimeException("Uninitialized vars not permitted here.");
      }
      Type type = parseType(tokenizer);
      return new UninitializedField(type, varName);
    } else {
      throw new RuntimeException(": or = expected");
    }
  }


  private RemStatement parseRem(Tokenizer tokenizer) {
    tokenizer.nextToken();
    StringBuilder sb = new StringBuilder();
    while (tokenizer.currentType != Tokenizer.TokenType.EOF) {
      sb.append(tokenizer.leadingWhitespace).append(tokenizer.currentValue);
      tokenizer.nextToken();
    }
    if (sb.length() > 0 && sb.charAt(0) == ' ') {
      sb.deleteCharAt(0);
    }
    return new RemStatement(sb.toString());
  }

  private ReturnStatement parseFunctionReturn(Tokenizer tokenizer) {
    tokenizer.nextToken();
    if (tokenizer.currentType != Tokenizer.TokenType.EOF &&
            !tokenizer.currentValue.equals(";")) {
      return new ReturnStatement(expressionParser.parse(tokenizer));
    }
    return new ReturnStatement();
  }


  public List<Statement> parseStatementList(Tokenizer tokenizer, FunctionImplementation parsingContext) {
    ArrayList<Statement> result = new ArrayList<>();
    Node statement;
    do {
      while (tokenizer.tryConsume(";")) {
        // result.add(new LegacyStatement(null));
      }
      if (tokenizer.currentType == Tokenizer.TokenType.EOF) {
        break;
      }
      parseStatement(tokenizer, result, parsingContext);
      statement = result.get(result.size() - 1);
    } while (statement instanceof BlockStatement
            || tokenizer.tryConsume(";"));
    if (tokenizer.currentType != Tokenizer.TokenType.EOF) {
      throw tokenizer.exception("Leftover input.", null);
    }
    return result;
  }

  void require(Tokenizer tokenizer, String s) {
    if (!tryConsume(tokenizer, s)) {
      throw tokenizer.exception("Expected: '" + s + "'.", null);
    }
  }

  boolean tryConsume(Tokenizer tokenizer, String s) {
    if (tokenizer.currentValue.equalsIgnoreCase(s)) {
      tokenizer.nextToken();
      return true;
    }
    return false;
  }


  public Type parseType(Tokenizer tokenizer) {
    String typeName = tokenizer.consumeIdentifier();
    if (typeName.equals("float")) {
      return Types.FLOAT;
    }
    if (typeName.equals("str")) {
      return Types.STR;
    } if (typeName.equals("bool")) {
      return Types.BOOL;
    }
    if (typeName.equals("List")) {
      tokenizer.consume("[");
      Type elementType = parseType(tokenizer);
      tokenizer.consume("]");
      return new ListType(elementType);
    }
    GlobalSymbol symbol = program.getSymbol(typeName);
    if (symbol == null) {
      throw new RuntimeException("Unrecognized type: " + typeName);
    }
    if (!(symbol.getValue() instanceof Type)) {
      throw new RuntimeException("'" + typeName + "' is not a type!");
    }
    return  (Type) symbol.getValue();
  }
}
