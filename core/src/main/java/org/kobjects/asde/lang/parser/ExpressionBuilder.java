package org.kobjects.asde.lang.parser;

import org.kobjects.asde.lang.node.AndOperator;
import org.kobjects.asde.lang.node.Apply;
import org.kobjects.asde.lang.node.ArrayAccess;
import org.kobjects.asde.lang.node.ArrayLiteral;
import org.kobjects.asde.lang.node.Colon;
import org.kobjects.asde.lang.node.Group;
import org.kobjects.asde.lang.node.ImpliedSliceValue;
import org.kobjects.asde.lang.node.InvokeMethod;
import org.kobjects.asde.lang.node.NegOperator;
import org.kobjects.asde.lang.node.Constructor;
import org.kobjects.asde.lang.node.Slice;
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
import org.kobjects.expressionparser.Processor;
import org.kobjects.expressionparser.Tokenizer;

import java.util.ArrayList;
import java.util.List;

/**
 * This class configures and manages the parser and is able to turn the expression parser
 * callbacks into an expression node tree.
 */
class ExpressionBuilder extends Processor<Node> {

  private final Program program;

  ExpressionBuilder(Program program) {
    this.program = program;
  }

  @Override
  public Node apply(Tokenizer tokenizer, Node base, String bracket, List<Node> arguments) {
    Node[] children = new Node[arguments.size() + 1];
    for (int i = 0; i < arguments.size(); i++) {
      children[i + 1] = arguments.get(i);
    }
    if (base instanceof Path) {
      Path path = (Path) base;
      children[0] = path.children[0];
      return new InvokeMethod(path.pathName, children);
    }
    children[0] = base;
    return new Apply(true, children);
  }

  @Override
  public Node prefixOperator(Tokenizer tokenizer, String name, Node param) {
    switch (name) {
      case "not":
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
  public Node suffixOperator(Tokenizer tokenizer, String name, Node base) {
    AsdeExpressionParser subParser = new AsdeExpressionParser(program);
    ArrayList<Node> children = new ArrayList<>();
    if (name.equals("{")) {
      if (!tokenizer.tryConsume("}")) {
        do {
          Node child = subParser.parse(tokenizer);
          if (tokenizer.tryConsume(":")) {
            child = new Colon(child, subParser.parse(tokenizer));
          }
          children.add(child);
        } while (tokenizer.tryConsume(","));
        tokenizer.consume("}");
      }
      return Constructor.create(base, children);
    }

    children.add(base);
    String separator = null;
    if (!tokenizer.tryConsume("]")) {
      do {
        if (!",".equals(separator) && tokenizer.tryConsume(":")) {
          children.add(new ImpliedSliceValue());
          separator = ":";
          if (tokenizer.currentValue.equals("]")) {
            children.add(new ImpliedSliceValue());
            break;
          }
          continue;
        }
        Node child = subParser.parse(tokenizer);
        children.add(child);
        if (separator == null) {
          if (tokenizer.currentValue.equals(":")) {
            separator = tokenizer.currentValue;
          } else {
            separator = ",";
          }
        }
      } while (tokenizer.tryConsume(separator));
      tokenizer.consume("]");
    }

    if (":".equals(separator)) {
      return new Slice(children.toArray(Node.EMPTY_ARRAY));
    }
    return new ArrayAccess(children.toArray(Node.EMPTY_ARRAY));
  }

  @Override
  public Node infixOperator(Tokenizer tokenizer, String name, Node left, Node right) {
    switch (name) {
      case ":":
        return new Colon(left, right);
      case ".":
        return new Path(left, right);
      case "<":
        return new RelationalOperator(-1, -1, left, right);
      case "<=":
      case "≤":
        return new RelationalOperator(-1, 0, left, right);
      case "==":
      case "=":
        return new RelationalOperator(0, 0, left, right);
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
      case "%":
        return new MathOperator(MathOperator.Kind.MOD, left, right);
      case "^":
        return new MathOperator(MathOperator.Kind.POW, left, right);
      case "and":
        return new AndOperator(left, right);
      case "or":
        return new OrOperator(left, right);
      default:
        return super.infixOperator(tokenizer, name, left, right);
    }
  }

  @Override
  public Node group(Tokenizer tokenizer, String bracket, List<Node> args) {
    switch (bracket) {
      case "(":
        return new Group(args.get(0));
      case "[":
        return new ArrayLiteral(args.toArray(new Node[0]));
      default:
        return super.group(tokenizer, bracket, args);
    }
  }

  @Override
  public Node identifier(Tokenizer tokenizer, String name) {

    switch(name) {
      case "true":
      case "True":
        return new Literal(Boolean.TRUE);
      case "false":
      case "False":
        return new Literal(Boolean.FALSE);
      case "self":
        return new Self();
    }
    return new Identifier(name);
  }

  @Override
  public Node numberLiteral(Tokenizer tokenizer, String value) {
    if (value.startsWith("0x")) {
      return new Literal((double) Long.parseLong(value.substring(2), 16), Literal.Format.HEX);
    }
    return new Literal(Double.parseDouble(value));
  }

  @Override
  public Node stringLiteral(Tokenizer tokenizer, String value) {
    return new Literal(value.substring(1, value.length()-1).replace("\"\"", "\""));
  }
}
