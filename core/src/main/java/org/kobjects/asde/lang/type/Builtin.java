package org.kobjects.asde.lang.type;

import org.kobjects.asde.lang.Documented;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.Program;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.FunctionTypeImpl;
import org.kobjects.typesystem.Parameter;
import org.kobjects.typesystem.Type;

public enum Builtin implements Function, Documented {

    ABS("Calculates the absolute value of the input",
        1, Types.NUMBER), ASC(1, Types.STRING),
    CHR$(1, Types.NUMBER), COS(1, Types.NUMBER),
    EXP(1, Types.NUMBER),
    INT(1, Types.NUMBER),
    LEFT$(2, Types.STRING, Types.NUMBER), LEN(1, Types.STRING), LOG(1, Types.NUMBER),
    MID$(2, Types.STRING, Types.NUMBER, Types.NUMBER),
    RIGHT$(2, Types.STRING, Types.NUMBER), RND(0, Types.NUMBER),
    SGN(1, Types.NUMBER), STR$(1, Types.NUMBER), SQR(1, Types.NUMBER), SIN(1, Types.NUMBER),
    TAB(1, Types.NUMBER), TAN(1, Types.NUMBER),
    VAL(1, Types.STRING);

  public static int asInt(Object o) {
    return Math.round(((Double) o).floatValue());
  }


  public int minParams;
  public FunctionType signature;
  private final String documentation;

    Builtin(String documentation, int minParams, Type... parameterTypes) {
      this.documentation = documentation;
      this.minParams = minParams;
      Parameter[] parameters = new Parameter[parameterTypes.length];
      for (int i = 0; i < parameters.length; i++) {
        parameters[i] = new Parameter(String.valueOf((char) ('a' + i)), parameterTypes[i]);
      }
      this.signature = new FunctionTypeImpl(name().endsWith("$") ? Types.STRING : Types.NUMBER, minParams, parameterTypes);
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
  public String getDocumentation() {
    return null;
  }
}
