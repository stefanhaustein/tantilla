package org.kobjects.asde.lang.parser;

import org.kobjects.asde.lang.expression.ExpressionNode;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.expressionparser.ExpressionParser;
import org.kobjects.expressionparser.OperatorType;
import org.kobjects.expressionparser.Tokenizer;

import java.util.Scanner;
import java.util.regex.Pattern;

public class AsdeExpressionParser extends ExpressionParser<ExpressionNode> {

  static final Pattern NUMBER_PATTERN = Pattern.compile(
      "\\G\\s*((0x[0-9a-fA-f]+)|(\\d+(\\.\\d*)?|\\.\\d+)([eE][+-]?\\d+)?)");
  static final Pattern LINE_COMMENT_PATTERN = Pattern.compile("\\G\\h*#.*(\\v|\\Z)");


  public AsdeExpressionParser(Program program) {
    super(new ExpressionBuilder(program));
//    addApplyBrackets(10,"(", ",", ")");
    addGroupBrackets("(", null, ")");
    addGroupBrackets("[", ",", "]");
    addOperators(OperatorType.INFIX, 15, ".");
    addOperators(OperatorType.SUFFIX, 14,"{", "[", "(");
    addOperators(OperatorType.INFIX, 13, "^", "**");
    addOperators(OperatorType.PREFIX, 12, "-", "~");
    addOperators(OperatorType.INFIX, 11, "*", "/", "×", "⋅", "÷", "%", "//");
    addOperators(OperatorType.INFIX, 10, "+", "-", "−");
    addOperators(OperatorType.INFIX, 9, "<<", ">>");
    addOperators(OperatorType.INFIX, 8, "&");
    addOperators(OperatorType.INFIX, 7, "^");
    addOperators(OperatorType.INFIX, 6, "|");
    addOperators(OperatorType.INFIX, 5, ">=", "<=", ">", "<", "≠", "!=", "≥", "≤", "==");
    addOperators(OperatorType.PREFIX, 4, "not");
    addOperators(OperatorType.INFIX, 3, "and");
    addOperators(OperatorType.INFIX, 2, "or");
    addOperators(OperatorType.PREFIX, 1, "lambda");
  }


  public Tokenizer createTokenizer(String line) {
    Tokenizer tokenizer = new Tokenizer(new Scanner(line), getSymbols(), "->", ";", ":", "}", "=");
    tokenizer.numberPattern = NUMBER_PATTERN;
    tokenizer.lineCommentPattern = LINE_COMMENT_PATTERN;
    return tokenizer;
  }

}
