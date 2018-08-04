package org.kobjects.asde.lang.parser;

import org.kobjects.asde.lang.node.Builtin;
import org.kobjects.asde.lang.node.New;
import org.kobjects.asde.lang.node.FnCall;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.node.Literal;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.node.Operator;
import org.kobjects.asde.lang.node.Path;
import org.kobjects.asde.lang.node.Variable;
import org.kobjects.asde.lang.type.Type;
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
  public Node call(ExpressionParser.Tokenizer tokenizer, String name, String bracket, List<Node> arguments) {
    Node[] children = arguments.toArray(new Node[arguments.size()]);
    for (Builtin.Kind builtinId: Builtin.Kind.values()) {
      if (name.equalsIgnoreCase(builtinId.name())) {
        String signature = builtinId.signature;
        if (arguments.size() > signature.length() ||
            arguments.size() < builtinId.minParams ) {
          throw new IllegalArgumentException("Parameter count mismatch.");
        }
        for (int i = 0; i < arguments.size(); i++) {
          if (signature.charAt(i) != Parser.returnTypeCode(arguments.get(i).returnType())) {
            throw new RuntimeException("Parameter number " + i + " kind mismatch.");
          }
        }
        return new Builtin(program, builtinId, children);
      }
    }
    name = name.toLowerCase();
    if (name.startsWith("fn") && name.length() > 2) {
      return new FnCall(program, name, children);
    }
    if (name.length() > 2) {
      System.out.println("Unsupported Function? " + name);
    }
    for (int i = 0; i < arguments.size(); i++) {
      if (arguments.get(i).returnType() != Type.NUMBER) {
        throw new IllegalArgumentException("Numeric array index expected.");
      }
    }
    return new Variable(program, name, children);
  }

  @Override
  public Node prefixOperator(ExpressionParser.Tokenizer tokenizer, String name, Node param) {
    if (param.returnType() != Type.NUMBER) {
      throw new IllegalArgumentException("Numeric argument expected for '" + name + "'.");
    }
    if (name.equalsIgnoreCase("NOT")) {
      return new Builtin(program, Builtin.Kind.NOT, param);
    }
    if (name.equals("-")) {
      return new Builtin(program, Builtin.Kind.NEG, param);
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
    if ("+<=<>=".indexOf(name) == -1 && (left.returnType() != Type.NUMBER ||
        right.returnType() != Type.NUMBER)) {
      throw new IllegalArgumentException("Numeric arguments expected for '" + name + "'.");
    }
    return new Operator(name.toLowerCase(), left, right);
  }

  @Override
  public Node group(ExpressionParser.Tokenizer tokenizer, String bracket, List<Node> args) {
    return new Builtin(program, null, args.get(0));
  }

  @Override public Node identifier(ExpressionParser.Tokenizer tokenizer, String name) {
    if (name.equalsIgnoreCase(Builtin.Kind.RND.name())) {
      return new Builtin(program, Builtin.Kind.RND);
    }

    name = name.toLowerCase();

    if (name.equals("new")) {
      String className = tokenizer.consumeIdentifier();
      return new New(program, className);
    }

    if (name.startsWith("fn") && name.length() > 2) {
      return new FnCall(program, name);
    }
    return new Variable(program, name);
  }

  @Override public Node numberLiteral(ExpressionParser.Tokenizer tokenizer, String value) {
    return new Literal(Double.parseDouble(value));
  }

  @Override
  public Node stringLiteral(ExpressionParser.Tokenizer tokenizer, String value) {
    return new Literal(value.substring(1, value.length()-1).replace("\"\"", "\""));
  }
}
