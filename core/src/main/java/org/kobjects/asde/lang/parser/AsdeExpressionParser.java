package org.kobjects.asde.lang.parser;

import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.expressionparser.ExpressionParser;

import java.util.Scanner;
import java.util.regex.Pattern;

public class AsdeExpressionParser extends ExpressionParser<Node> {

  public AsdeExpressionParser(Program program) {
    super(new ExpressionBuilder(program));
    addApplyBrackets(9,"(", ",", ")");
    addApplyBrackets(9,"[", ",", "]");  // HP
    addGroupBrackets("(", null, ")");
    addGroupBrackets("{", ",", "}");
    addOperators(ExpressionParser.OperatorType.INFIX, 10, ".");
    addOperators(ExpressionParser.OperatorType.INFIX, 8, "^");
    addOperators(ExpressionParser.OperatorType.PREFIX, 7, "-");
    addOperators(ExpressionParser.OperatorType.INFIX, 6, "*", "/");
    addOperators(ExpressionParser.OperatorType.INFIX, 5, "+", "-");
    addOperators(ExpressionParser.OperatorType.INFIX, 4, ">=", "<=", "<>", ">", "<", "=");
    addOperators(ExpressionParser.OperatorType.PREFIX, 3, "not", "NOT", "Not");
    addOperators(ExpressionParser.OperatorType.INFIX, 2, "and", "AND", "And");
    addOperators(ExpressionParser.OperatorType.INFIX, 1, "or", "OR", "Or");
  }


  public ExpressionParser.Tokenizer createTokenizer(String line) {
    ExpressionParser.Tokenizer tokenizer = new ExpressionParser.Tokenizer(new Scanner(line), getSymbols(), "->", ";", ":");
    tokenizer.numberPattern = Pattern.compile(
        "\\G\\s*((#[0-9a-fA-f]+)|(\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?)");
    tokenizer.lineCommentPattern = Pattern.compile("\\G\\h*'.*(\\v|\\Z)");
    return tokenizer;
  }

}
