package org.kobjects.asde.lang;

import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Parameter;
import org.kobjects.typesystem.Type;

public enum Builtin implements Function {

    ABS(1, Types.NUMBER), ASC(1, Types.STRING),
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

    Builtin(int minParams, Type... parameterTypes) {
      this.minParams = minParams;
      Parameter[] parameters = new Parameter[parameterTypes.length];
      for (int i = 0; i < parameters.length; i++) {
        parameters[i] = new Parameter(String.valueOf((char) ('a' + i)), parameterTypes[i]);
      }
      this.signature = new FunctionType(name().endsWith("$") ? Types.STRING : Types.NUMBER, parameterTypes);
    }

  @Override
  public FunctionType getType() {
    return signature;
  }

  public int getLocalVariableCount() {
      return signature.getParameterCount();
  }

  public Object eval(Interpreter interpreter, Object[] params) {
    switch (this) {
      case ABS: return Math.abs((Double) params[0]);
      case ASC: {
        String s = (String) params[0];
        return s.length() == 0 ? 0.0 : (double) s.charAt(0);
      }
      case CHR$: return String.valueOf((char) ((Double)(params[0])).intValue());
      case COS: return Math.cos((Double) params[0]);
      case EXP: return Math.exp((Double) params[0]);
      case INT: return Math.floor((Double) params[0]);
      case LEFT$: {
        String s = (String) params[0];
        return s.substring(0, Math.min(s.length(), asInt(params[1])));
      }
      case LEN: return (double) ((String) params[0]).length();
      case LOG: return Math.log((Double) params[0]);
      case MID$: {
        String s = (String) params[0];
        int start = Math.max(0, Math.min(asInt(params[1]) - 1, s.length()));
        if (params.length == 2) {
          return s.substring(start);
        }
        int count = asInt(params[2]);
        int end = Math.min(s.length(), start + count);
        return s.substring(start, end);
      }
      case SGN: return Math.signum((Double) params[0]);
      case SIN: return Math.sin((Double) params[0]);
      case SQR: return Math.sqrt((Double) params[0]);
      case STR$: return Program.toString(params[0]);
      case RIGHT$: {
        String s = (String) params[0];
        return s.substring(Math.min(s.length(), s.length() - asInt(params[1])));
      }
      case RND: return Math.random();
      case TAB: return interpreter.program.tab(asInt(params[1]));
      case TAN: return Math.tan((Double) params[0]);
      case VAL: return Double.parseDouble((String) params[0]);
      default:
        throw new IllegalArgumentException("NYI: " + name());
    }
  }

}
