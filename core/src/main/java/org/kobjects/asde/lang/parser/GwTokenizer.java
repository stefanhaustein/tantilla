package org.kobjects.asde.lang.parser;

import org.kobjects.asde.lang.node.Statement;
import org.kobjects.expressionparser.ExpressionParser;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A tokenizer subclass that splits identifiers if they contain reserved words,
 * so it will report "IFA<4THENPRINTZ" as "IF" "A" "<" "4" "THEN" "PRINT" "Z"
 */
class GwTokenizer extends ExpressionParser.Tokenizer {
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

    if (currentType == TokenType.IDENTIFIER) {
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
