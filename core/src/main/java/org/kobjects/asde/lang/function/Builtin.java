package org.kobjects.asde.lang.function;

import org.kobjects.annotatedtext.AnnotatedString;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.FunctionTypeImpl;
import org.kobjects.typesystem.Parameter;
import org.kobjects.typesystem.Type;

public enum Builtin implements Function {

    ABS("Calculates the absolute value of the input.\n\nExamples:\n\n * abs(3.4) = 3.4\n * abs(-4) = 4\n * abs(0) = 0",
        1, Types.NUMBER),
    ASC("Returns the ascii value of the first character of the string\n\nExample:\n\n * asc(\"A\") = 65.",
        1, Types.STRING),
    ATAN2("Converts the given cartesian coordinates into the angle of the corresponding polar coordinates", 2, Types.NUMBER, Types.NUMBER),
    CHR$("Returns a single-character string representing the given ASCII value.\n\nExample:\n\n * chr$(65) = \"A\"",
        1, Types.NUMBER),
    COS("Calculates the cosine of the parameter value.",
        1, Types.NUMBER),
    EXP("Returns e raised to the power of the parameter value.",
        1, Types.NUMBER),
    INT("Rounds down to the next lower integer",
        1, Types.NUMBER),
    LEFT$("Returns the prefix of the string consisting of the given number of characters.\n\n"
        + "Example:\n\nleft$(\"abcdefg\", 3) = \"abc\"",
        2, Types.STRING, Types.NUMBER),
    LEN("Returns the length of the given string.\n\nExample:\n\n * len(\"ABC\") = 3",
        1, Types.STRING),
    LOG("Calculates the logarithm to the base e.",
        1, Types.NUMBER),
    MID$("Returns the substring starting at the given position (1-based) with the given length. "
        + "If the length is omitted, the whole remainder is returned.\n\n"
        + "Examples:\n\n"
        + " * mid$(\"abcdefg\", 3, 2) = \"cd\"\n"
        + " * mid$(\"abcdefg\", 3) = \"cdefg\"",
        2, Types.STRING, Types.NUMBER, Types.NUMBER),
    RIGHT$("Returns the suffix of the string with the given number of characters.\n\n"
        + "Example:\n\n * right$(\"abc\", 2) = \"bc\"",
        2, Types.STRING, Types.NUMBER),
    RND("Returns a (pseudo-)random number in the range from 0 (inclusive) to 1 (exclusive)",
        0, Types.NUMBER, Types.NUMBER),
    SGN("Returns the sign of the given number: 1 for positive numbers, 0 for zero and -1 for negative numbers.",
        1, Types.NUMBER),
    STR$("Converts the given number to a string (simiar to PRINT, but without any leading spaces.",
        1, Types.NUMBER),
    SQR("Calculates the square root of the argument\n\nExample:\n\n * sqr(9) = 3", 1, Types.NUMBER),
    SIN("Calculates the sine of the parameter value.", 1, Types.NUMBER),
    TAB("Returns a string with the given number of spaces, relative to the start of the current line, taking the current cursor position into account. The string will be empty if the cursor is at or beyond the given position", 1, Types.NUMBER),
    TAN("Calculates the tangent of the argument", 1, Types.NUMBER),
    VAL("Parses the argument as a floating point number. If this fails, the return value is 0.", 1, Types.STRING);

  public static int asInt(Object o) {
    return Math.round(((Double) o).floatValue());
  }

  private static String left(String s, int count) {
    int pos = 0;
    for (int i = 0; i < count && pos < s.length(); i++) {
      int cp = Character.codePointAt(s, pos);
      pos += Character.charCount(cp);
    }
    return pos >= s.length() ? s : s.substring(0, pos);
  }

  private static int len(String s) {
    int pos = 0;
    int count = 0;
    int len = s.length();
    while (pos < len) {
      int cp = Character.codePointAt(s, pos);
      pos += Character.charCount(cp);
      count++;
    }
    return count;
  }

  private static String mid(String s, int start) {
    int pos = 0;
    int len = s.length();
    for (int i = 1; i < start && pos < len; i++) {
      int cp = Character.codePointAt(s, pos);
      pos += Character.charCount(cp);
    }
    return pos >= len ? "" : s.substring(pos);
  }

  private static String right(String s, int count) {
    return mid(s, len(s) - count + 1);
  }

  private static String mid(String s, int start, int count) {
    int p0 = 0;
    int len = s.length();
    for (int i = 1; i < start && p0 < len; i++) {
      int cp = Character.codePointAt(s, p0);
      p0 += Character.charCount(cp);
    }
    int p1 = p0;
    while (p1 < len && count > 0) {
      int cp = Character.codePointAt(s, p1);
      p1 += Character.charCount(cp);
      count--;
    }
    return p0 >= len ? "" : s.substring(p0, Math.min(p1, len));
  }

  public int minParams;
  public FunctionType signature;
  private final AnnotatedString documentation;

    Builtin(String documentation, int minParams, Type... parameterTypes) {
      this.documentation = AnnotatedString.of(documentation);
      this.minParams = minParams;
      Parameter[] parameters = new Parameter[parameterTypes.length];
      for (int i = 0; i < parameters.length; i++) {
        parameters[i] = new Parameter(String.valueOf((char) ('a' + i)), parameterTypes[i]);
      }
      this.signature = new FunctionTypeImpl((name().endsWith("$") || name().equalsIgnoreCase("TAB")) ? Types.STRING : Types.NUMBER, minParams, parameterTypes);
    }

  Builtin(int minParams, Type... parameterTypes) {
    this(null, minParams, parameterTypes);
  }


  @Override
  public FunctionType getType() {
    return signature;
  }

  public Object call(EvaluationContext evaluationContext, int paramCount) {
    switch (this) {
      case ABS:
        return Math.abs((Double) evaluationContext.getParameter(0));
      case ATAN2:
        return Math.atan2((Double) evaluationContext.getParameter(0), (Double) evaluationContext.getParameter(1));
      case ASC: {
        String s = (String) evaluationContext.getParameter(0);
        return s.length() == 0 ? 0.0 : (double) Character.codePointAt(s, 0);
      }
      case CHR$:
        return String.valueOf(Character.toChars(((Double) (evaluationContext.getParameter(0))).intValue()));
      case COS:
        return Math.cos((Double) evaluationContext.getParameter(0));
      case EXP:
        return Math.exp((Double) evaluationContext.getParameter(0));
      case INT:
        return Math.floor((Double) evaluationContext.getParameter(0));
      case LEFT$:
        return left((String) evaluationContext.getParameter(0), asInt(evaluationContext.getParameter(1)));
      case LEN:
        return Double.valueOf(len((String) evaluationContext.getParameter(0)));
      case LOG:
        return Math.log((Double) evaluationContext.getParameter(0));
      case MID$: {
        String s = (String) evaluationContext.getParameter(0);
        int start = asInt(evaluationContext.getParameter(1));
        if (paramCount == 2) {
          return mid(s, start);
        }
        return mid(s, start, asInt(evaluationContext.getParameter(2)));
      }
      case SGN:
        return Math.signum((Double) evaluationContext.getParameter(0));
      case SIN:
        return Math.sin((Double) evaluationContext.getParameter(0));
      case SQR:
        return Math.sqrt((Double) evaluationContext.getParameter(0));
      case STR$:
        return Program.toString(evaluationContext.getParameter(0));
      case RIGHT$: {
        return right(
            (String) evaluationContext.getParameter(0),
            asInt(evaluationContext.getParameter(1)));
      }
      case RND:
        return Math.random();
      case TAB:
        return evaluationContext.control.program.tab(asInt(evaluationContext.getParameter(0)));
      case TAN:
        return Math.tan((Double) evaluationContext.getParameter(0));
      case VAL:
        return Double.parseDouble((String) evaluationContext.getParameter(0));
      default:
        throw new IllegalArgumentException("NYI: " + name());
    }
  }

  @Override
  public AnnotatedString getDocumentation() {
    return documentation;
  }
}
