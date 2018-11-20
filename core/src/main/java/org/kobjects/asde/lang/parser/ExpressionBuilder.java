package org.kobjects.asde.lang.parser;

import org.kobjects.asde.lang.node.Apply;
import org.kobjects.asde.lang.node.Group;
import org.kobjects.asde.lang.node.New;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.node.Literal;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.node.Operator;
import org.kobjects.asde.lang.node.Path;
import org.kobjects.asde.lang.node.Identifier;
import org.kobjects.typesystem.Type;
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
    if (name.equalsIgnoreCase("not") || name.equals("-")) {
      return new Operator(name, param);
    }
    if (name.equals("+")) {
      return param;
    }
    return super.prefixOperator(tokenizer, name, param);
  }

  @Override
  public Node infixOperator(ExpressionParser.Tokenizer tokenizer, String name, Node left, Node right) {
    if (name.equals(".")) {
      return new Path(left, right);
    }
    return new Operator(name.toLowerCase(), left, right);
  }

  @Override
  public Node group(ExpressionParser.Tokenizer tokenizer, String bracket, List<Node> args) {
    return new Group(args.get(0));
  }

  @Override
  public Node identifier(ExpressionParser.Tokenizer tokenizer, String name) {
    name = name.toLowerCase();

    if (name.equals("new")) {
      String className = tokenizer.consumeIdentifier();
      return new New(program, className);
    }

    return new Identifier(program, name);
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
