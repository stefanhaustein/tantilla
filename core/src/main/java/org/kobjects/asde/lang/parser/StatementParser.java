package org.kobjects.asde.lang.parser;

import org.kobjects.asde.lang.classifier.trait.AdapterType;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.function.Parameter;
import org.kobjects.asde.lang.list.ListType;
import org.kobjects.asde.lang.function.UserFunction;
import org.kobjects.asde.lang.node.Identifier;
import org.kobjects.asde.lang.node.InvokeNamed;
import org.kobjects.asde.lang.node.Path;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.node.Invoke;
import org.kobjects.asde.lang.node.Group;
import org.kobjects.asde.lang.node.NegOperator;
import org.kobjects.asde.lang.statement.AssignStatement;
import org.kobjects.asde.lang.statement.BlockStatement;
import org.kobjects.asde.lang.statement.ConditionStatement;
import org.kobjects.asde.lang.statement.DebuggerStatement;
import org.kobjects.asde.lang.statement.EndStatement;
import org.kobjects.asde.lang.statement.ForStatement;
import org.kobjects.asde.lang.statement.LaunchStatement;
import org.kobjects.asde.lang.statement.OnChangeStatement;
import org.kobjects.asde.lang.statement.PrintStatement;
import org.kobjects.asde.lang.statement.AssignmentStatement;
import org.kobjects.asde.lang.node.Literal;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.node.MathOperator;
import org.kobjects.asde.lang.statement.OnStatement;
import org.kobjects.asde.lang.statement.RemStatement;
import org.kobjects.asde.lang.statement.ReturnStatement;
import org.kobjects.asde.lang.statement.Statement;
import org.kobjects.asde.lang.statement.VoidStatement;
import org.kobjects.asde.lang.type.AwaitableType;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.statement.WhileStatement;
import org.kobjects.expressionparser.Tokenizer;
import org.kobjects.asde.lang.type.Type;

import java.util.ArrayList;
import java.util.List;

public class StatementParser {
  public final AsdeExpressionParser expressionParser;
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


  private Parameter[] parseParameterList(Tokenizer tokenizer, Type self) {
    tokenizer.consume("(");
    ArrayList<Parameter> parameters = new ArrayList<>();
    while (!tokenizer.tryConsume(")")) {
      String parameterName = tokenizer.consumeIdentifier();
      if (parameterName.equals("self")) {
        if (parameters.size() != 0) {
          throw new RuntimeException("self must be first parameter");
        }
        if (self == null) {
          throw new RuntimeException("The parameter name 'self' is reserved for class and trait methods.");
        }
        if (tokenizer.tryConsume(":")) {
          Type parameterType = parseType(tokenizer);
          if (parameterType != self) {
            throw new RuntimeException("Type mismatch for self. Expected: " + self + " got: " + parameterType);
          }
        }
        parameters.add(Parameter.create("self", self instanceof AdapterType ? ((AdapterType) self).classType : self));
      } else {
        tokenizer.consume(":");
        Type parameterType = parseType(tokenizer);
        parameters.add(Parameter.create(parameterName, parameterType));
      }
      if (!tokenizer.tryConsume(",")) {
        if (tokenizer.tryConsume(")")) {
          break;
        }
        throw new RuntimeException("',' or ')' expected.");
      }
    }
    return parameters.toArray(Parameter.EMPTY_ARRAY);
  }

  public FunctionType parseFunctionSignature(Tokenizer tokenizer, boolean async, Type self) {
    Parameter[] parameterTypes = parseParameterList(tokenizer, self);

    Type returnType = tokenizer.tryConsume("->") ? parseType(tokenizer) : Types.VOID;

    return new FunctionType(async ? new AwaitableType(returnType) : returnType, parameterTypes);
  }

  void parseStatement(Tokenizer tokenizer, List<Statement> result, UserFunction parsingContext) {
    String name = tokenizer.currentValue;

    switch (name.toLowerCase()) {
      case "const":
      case "let":
      case "mut":
        result.add(parseDeclaration(tokenizer));
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
      case "launch":
        tokenizer.consumeIdentifier();
        result.add(new LaunchStatement(expressionParser.parse(tokenizer)));
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

    Node expression = expressionParser.parse(tokenizer);
    if (tokenizer.tryConsume("=")) {
      boolean await = tokenizer.tryConsume("await");
      try {
        result.add(AssignmentStatement.createAssignment(expression, await, expressionParser.parse(tokenizer)));
      } catch (Exception e) {
        throw tokenizer.exception(null, e);
      }
    } else if (!tokenizer.currentValue.equals(";") && !tokenizer.currentValue.equals("")) {

      // Extra parameters without parenthesis

      List<Node> params = new ArrayList<>();
      if (tokenizer.tryConsume(",")) {
        if (expression instanceof MathOperator && ((MathOperator) expression).kind == MathOperator.Kind.SUB) {
          params.add(expression.children[0]);
          params.add(new Group(new NegOperator(expression.children[1])));
        } else if ((expression instanceof Invoke || expression instanceof InvokeNamed) && expression.children.length == 2) {
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

      if (expression instanceof InvokeNamed) {
        InvokeNamed invokeNamed = (InvokeNamed) expression;
        result.add(new VoidStatement(new InvokeNamed(invokeNamed.name, invokeNamed.mainModule, params.toArray(Node.EMPTY_ARRAY))));
      } else if (params.get(0) instanceof Path) {
        Path path = (Path) params.get(0);
        params.set(0, path.children[0]);
        result.add(new VoidStatement(new InvokeNamed(path.pathName, false, params.toArray(Node.EMPTY_ARRAY))));
      } else if (params.get(0) instanceof Identifier) {
        result.add(new VoidStatement(new InvokeNamed(params.get(0).toString(), true, params.subList(1, params.size()).toArray(Node.EMPTY_ARRAY))));
      } else {
        result.add(new VoidStatement(new Invoke(false, params.toArray(Node.EMPTY_ARRAY))));
      }
    } else {//if (expression instanceof Path || expression instanceof Identifier){
      result.add(new VoidStatement(expression));
    } /*else {
      result.add(expression);
    }*/
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

  AssignmentStatement parseDeclaration(Tokenizer tokenizer) {
    AssignmentStatement.Kind kind;
    if (tokenizer.tryConsume("mut")) {
      kind = AssignmentStatement.Kind.MUT;
    } else if (tokenizer.tryConsume("let")) {
      kind = tokenizer.tryConsume("mut") ? AssignmentStatement.Kind.MUT : AssignmentStatement.Kind.LET;
    } else {
      throw new RuntimeException("let or mut expected");
    }
    String varName = tokenizer.consumeIdentifier();
    tokenizer.consume("=");
    boolean await = tokenizer.tryConsume("await");
    Node value = expressionParser.parse(tokenizer);
    return AssignmentStatement.createDeclaration(kind, varName, await, value);
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


  public List<Statement> parseStatementList(Tokenizer tokenizer, UserFunction parsingContext) {
    ArrayList<Statement> result = new ArrayList<>();
    Node statement;
    do {
      while (tokenizer.tryConsume(";")) {
        // result.add(new LegacyStatement(null));
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
    Property symbol = program.mainModule.getProperty(typeName);
    if (symbol == null) {
      throw new RuntimeException("Unrecognized type: " + typeName);
    }
    if (!(symbol.getStaticValue() instanceof Type)) {
      throw new RuntimeException("'" + typeName + "' is not a type!");
    }
    return  (Type) symbol.getStaticValue();
  }
}
