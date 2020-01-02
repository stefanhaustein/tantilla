package org.kobjects.asde.lang.parser;

import org.kobjects.asde.lang.node.AndOperator;
import org.kobjects.asde.lang.node.Apply;
import org.kobjects.asde.lang.node.ArrayLiteral;
import org.kobjects.asde.lang.node.Group;
import org.kobjects.asde.lang.node.NegOperator;
import org.kobjects.asde.lang.node.New;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.node.Literal;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.node.MathOperator;
import org.kobjects.asde.lang.node.NotOperator;
import org.kobjects.asde.lang.node.OrOperator;
import org.kobjects.asde.lang.node.Path;
import org.kobjects.asde.lang.node.Identifier;
import org.kobjects.asde.lang.node.RelationalOperator;
import org.kobjects.asde.lang.node.Self;
import org.kobjects.expressionparser.ExpressionParser;

import java.util.List;

/**
 * This class configures and manages the parser and is able to turn the expression parser
 * callbacks into an expression node tree.
 */
class ExpressionBuilder extends ExpressionParser.Processor<Node> {

  private final Program program;

  ExpressionBuilder(Program program) {
    this.program = program;
  }

  @Override
  public Node apply(ExpressionParser.Tokenizer tokenizer, Node base, String bracket, List<Node> arguments) {
    Node[] children = new Node[arguments.size() + 1];
    children[0] = base;
    for (int i = 0; i < arguments.size(); i++) {
      children[i + 1] = arguments.get(i);
    }
    return new Apply(true, children);
  }

  @Override
  public Node prefixOperator(ExpressionParser.Tokenizer tokenizer, String name, Node param) {
    switch (name.toUpperCase()) {
      case "NOT":
        return new NotOperator(param);
      case "−":
      case "-":
        return new NegOperator(param);
      case "+":
        return param;
      default:
        return super.prefixOperator(tokenizer, name, param);
    }
  }

  @Override
  public Node infixOperator(ExpressionParser.Tokenizer tokenizer, String name, Node left, Node right) {
    switch (name.toUpperCase()) {
      case ".":
        return new Path(left, right);
      case "<":
        return new RelationalOperator(-1, -1, left, right);
      case "<=":
      case "≤":
        return new RelationalOperator(-1, 0, left, right);
      case "=":
        return new RelationalOperator(0, 0, left, right);
      case "<>":
      case "≠":
      case "!=":
        return new RelationalOperator(-1, 1, left, right);
      case ">":
        return new RelationalOperator(1, 1, left, right);
      case ">=":
      case "≥":
        return new RelationalOperator(1, 0, left, right);
      case "+":
        return new MathOperator(MathOperator.Kind.ADD, left, right);
      case "-":
      case "−":
        return new MathOperator(MathOperator.Kind.SUB, left, right);
      case "⋅":
      case "×":
      case "*":
        return new MathOperator(MathOperator.Kind.MUL, left, right);
      case "÷":
      case "/":
        return new MathOperator(MathOperator.Kind.DIV, left, right);
      case "^":
        return new MathOperator(MathOperator.Kind.POW, left, right);
      case "AND":
        return new AndOperator(left, right);
      case "OR":
        return new OrOperator(left, right);
      default:
        return super.infixOperator(tokenizer, name, left, right);
    }
  }

  @Override
  public Node group(ExpressionParser.Tokenizer tokenizer, String bracket, List<Node> args) {
    switch (bracket) {
      case "(":
        return new Group(args.get(0));
      case "{":
        return new ArrayLiteral(args.toArray(new Node[0]));
      default:
        return super.group(tokenizer, bracket, args);
    }
  }

  @Override
  public Node identifier(ExpressionParser.Tokenizer tokenizer, String name) {

    switch(name.toUpperCase()) {
      case "NEW":
        String className = tokenizer.currentValue;
        try {
          Node result = new New(className);
          tokenizer.consumeIdentifier();
          return result;
        } catch (Exception e) {
          throw tokenizer.exception(e.getMessage(), e);
        }
      case "TRUE":
        return new Literal(Boolean.TRUE);
      case "FALSE":
        return new Literal(Boolean.FALSE);
      case "SELF":
        return new Self();
    }
    return new Identifier(name);
  }

  @Override
  public Node numberLiteral(ExpressionParser.Tokenizer tokenizer, String value) {
    if (value.startsWith("#")) {
      return new Literal((double) Long.parseLong(value.substring(1), 16), Literal.Format.HEX);
    }
    return new Literal(Double.parseDouble(value));
  }

  @Override
  public Node stringLiteral(ExpressionParser.Tokenizer tokenizer, String value) {
    return new Literal(value.substring(1, value.length()-1).replace("\"\"", "\""));
  }
}
