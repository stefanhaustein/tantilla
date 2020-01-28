package org.kobjects.asde.lang.function;

import org.kobjects.annotatedtext.AnnotatedString;
import org.kobjects.asde.lang.array.Array;
import org.kobjects.asde.lang.array.ArrayType;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.FunctionTypeImpl;
import org.kobjects.typesystem.Parameter;
import org.kobjects.typesystem.Type;

public enum Builtin implements Function {

    ABS("Calculates the absolute value of the input.\n\nExamples:\n\n * abs(3.4) = 3.4\n * abs(-4) = 4\n * abs(0) = 0",
        Types.FLOAT, Types.FLOAT),
    ORD("Returns the code point value of the first character of the string\n\nExample:\n\n * ord(\"A\") = 65.",
        Types.FLOAT, Types.STR),
    ATAN2("Converts the given cartesian coordinates into the angle of the corresponding polar coordinates",
        Types.FLOAT, Types.FLOAT, Types.FLOAT),
    CHR("Returns a single-character string representing the given ASCII value.\n\nExample:\n\n * chr$(65) = \"A\"",
        Types.STR, Types.FLOAT),
    COS("Calculates the cosine of the parameter value.",
        Types.FLOAT, Types.FLOAT),
    EXP("Returns e raised to the power of the parameter value.",
        Types.FLOAT, Types.FLOAT),
    CEIL("Rounds up to the next higher integer",
        Types.FLOAT, Types.FLOAT),
    INT("Rounds down to the next lower integer",
      Types.FLOAT, Types.FLOAT),
    FLOOR("Rounds down to the next lower integer",
        Types.FLOAT, 1, Types.FLOAT),
    /*LEFT$("Returns the prefix of the string consisting of the given number of characters.\n\n"
        + "Example:\n\nleft$(\"abcdefg\", 3) = \"abc\"",
        Types.STR, 2, Types.STR, Types.FLOAT), */
    LEN("Returns the length of the given string.\n\nExample:\n\n * len(\"ABC\") = 3",
        Types.FLOAT, 1, Types.STR),
    LOG("Calculates the logarithm to the base e.",
        Types.FLOAT, 1, Types.FLOAT),
    RANGE("Returns a sequence of integers from the first parameter (inclusive) to the second parameter (exclusive)",
        new ArrayType(Types.FLOAT), 1, Types.FLOAT, Types.FLOAT, Types.FLOAT),
    RANDOM("Returns a (pseudo-)random number in the range from 0 (inclusive) to 1 (exclusive)",
        Types.FLOAT),
    STR("Converts the given number to a string (similar to print).",
        Types.STR, 1, Types.FLOAT),
    SQRT("Calculates the square root of the argument\n\nExample:\n\n * sqr(9) = 3",
        Types.FLOAT ,1, Types.FLOAT),
    SIN("Calculates the sine of the parameter value.", Types.FLOAT, Types.FLOAT),
    TAN("Calculates the tangent of the argument", Types.FLOAT, Types.FLOAT),
    FLOAT("Parses the argument as a floating point number. If this fails, the return value is 0.",
        Types.FLOAT, Types.STR);

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

    Builtin(String documentation, Type returnType, int minParams, Type... parameterTypes) {
      this.documentation = AnnotatedString.of(documentation);
      this.minParams = minParams;
      Parameter[] parameters = new Parameter[parameterTypes.length];
      for (int i = 0; i < parameters.length; i++) {
        parameters[i] = new Parameter(String.valueOf((char) ('a' + i)), parameterTypes[i]);
      }
      this.signature = new FunctionTypeImpl(returnType, minParams, parameterTypes);
    }

  Builtin(String documentation, Type returnType, Type... parameterTypes) {
    this(documentation, returnType, parameterTypes.length, parameterTypes);
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
      case ORD: {
        String s = (String) evaluationContext.getParameter(0);
        return s.length() == 0 ? 0.0 : (double) Character.codePointAt(s, 0);
      }
      case CEIL:
        return Math.ceil((Double) evaluationContext.getParameter(0));
      case CHR:
        return String.valueOf(Character.toChars(((Double) (evaluationContext.getParameter(0))).intValue()));
      case COS:
        return Math.cos((Double) evaluationContext.getParameter(0));
      case EXP:
        return Math.exp((Double) evaluationContext.getParameter(0));
      case FLOOR:
        return Math.floor((Double) evaluationContext.getParameter(0));
      case INT:
        return Double.valueOf((int) evaluationContext.getParameter(0));
      case LEN:
        return Double.valueOf(len((String) evaluationContext.getParameter(0)));
      case LOG:
        return Math.log((Double) evaluationContext.getParameter(0));
       case SIN:
        return Math.sin((Double) evaluationContext.getParameter(0));
      case SQRT:
        return Math.sqrt((Double) evaluationContext.getParameter(0));
      case STR:
        return Program.toString(evaluationContext.getParameter(0));
      case RANGE: {
        double end = (Double) evaluationContext.getParameter(paramCount == 1 ? 0 : 1);
        double start = paramCount < 2 ? 0 : (Double) evaluationContext.getParameter(1);
        double step = paramCount < 3 ? 1 : (Double) evaluationContext.getParameter(2);

        Object[] values = new Object[(int) ((end - start) / step)];
        for (int i = 0; i < values.length; i++) {
          values[i] = Double.valueOf(start + i * step);
        }
        return new Array(Types.FLOAT, values);
      }
      case RANDOM:
        return Math.random();
      case TAN:
        return Math.tan((Double) evaluationContext.getParameter(0));
      case FLOAT:
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
