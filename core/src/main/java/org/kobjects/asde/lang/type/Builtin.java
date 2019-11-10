package org.kobjects.asde.lang.type;

import org.kobjects.annotatedtext.AnnotatedString;
import org.kobjects.asde.lang.Documented;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.Program;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.FunctionTypeImpl;
import org.kobjects.typesystem.Parameter;
import org.kobjects.typesystem.Type;

public enum Builtin implements Function {

    ABS("Calculates the absolute value of the input.\n\nExamples:\n\n * abs(3.4) = 3.4\n * abs(-4) = 4\n * abs(0) = 0",
        1, Types.NUMBER),
    ASC("Returns the ascii value of the first character of the string\n\nExample:\n\n * asc(\"A\") = 65.",
        1, Types.STRING),
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
        0, Types.NUMBER),
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


  public int minParams;
  public FunctionType signature;
  private final AnnotatedString documentation;

    Builtin(String documentation, int minParams, Type... parameterTypes) {
      this.documentation = documentation == null ? null : new AnnotatedString(documentation);
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
      case ABS: return Math.abs((Double) evaluationContext.getParameter(0));
      case ASC: {
        String s = (String) evaluationContext.getParameter(0);
        return s.length() == 0 ? 0.0 : (double) s.charAt(0);
      }
      case CHR$: return String.valueOf((char) ((Double)(evaluationContext.getParameter(0))).intValue());
      case COS: return Math.cos((Double) evaluationContext.getParameter(0));
      case EXP: return Math.exp((Double) evaluationContext.getParameter(0));
      case INT: return Math.floor((Double) evaluationContext.getParameter(0));
      case LEFT$: {
        String s = (String) evaluationContext.getParameter(0);
        return s.substring(0, Math.min(s.length(), asInt(evaluationContext.getParameter(1))));
      }
      case LEN: return Double.valueOf(((String) evaluationContext.getParameter(0)).length());
      case LOG: return Math.log((Double) evaluationContext.getParameter(0));
      case MID$: {
        String s = (String) evaluationContext.getParameter(0);
        int start = Math.max(0, Math.min(asInt(evaluationContext.getParameter(1)) - 1, s.length()));
        if (paramCount == 2) {
          return s.substring(start);
        }
        int count = asInt(evaluationContext.getParameter(2));
        int end = Math.min(s.length(), start + count);
        return s.substring(start, end);
      }
      case SGN: return Math.signum((Double) evaluationContext.getParameter(0));
      case SIN: return Math.sin((Double) evaluationContext.getParameter(0));
      case SQR: return Math.sqrt((Double) evaluationContext.getParameter(0));
      case STR$: return Program.toString(evaluationContext.getParameter(0));
      case RIGHT$: {
        String s = (String) evaluationContext.getParameter(0);
        return s.substring(Math.min(s.length(), s.length() - asInt(evaluationContext.getParameter(1))));
      }
      case RND: return Math.random();
      case TAB: return evaluationContext.control.program.tab(asInt(evaluationContext.getParameter(0)));
      case TAN: return Math.tan((Double) evaluationContext.getParameter(0));
      case VAL: return Double.parseDouble((String) evaluationContext.getParameter(0));
      default:
        throw new IllegalArgumentException("NYI: " + name());
    }
  }

  @Override
  public AnnotatedString getDocumentation() {
    return documentation;
  }
}
