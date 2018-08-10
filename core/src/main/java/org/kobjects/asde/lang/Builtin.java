package org.kobjects.asde.lang;

import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Parameter;
import org.kobjects.typesystem.Type;

public enum Builtin implements Function {

    ABS(1, Type.NUMBER), ASC(1, Type.STRING),
    CHR$(1, Type.NUMBER), COS(1, Type.NUMBER),
    EXP(1, Type.NUMBER),
    INT(1, Type.NUMBER),
    LEFT$(2, Type.STRING, Type.NUMBER), LEN(1, Type.STRING), LOG(1, Type.NUMBER),
    MID$(2, Type.STRING, Type.NUMBER, Type.NUMBER),
    RIGHT$(2, Type.STRING, Type.NUMBER), RND(0, Type.NUMBER),
    SGN(1, Type.NUMBER), STR$(1, Type.NUMBER), SQR(1, Type.NUMBER), SIN(1, Type.NUMBER),
    TAB(1, Type.NUMBER), TAN(1, Type.NUMBER),
    VAL(1, Type.STRING);

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
      this.signature = new FunctionType(name().endsWith("$") ? Type.STRING : Type.NUMBER);
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
